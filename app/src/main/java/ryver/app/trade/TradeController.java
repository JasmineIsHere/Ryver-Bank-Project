package ryver.app.trade;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;

import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;
import ryver.app.customer.CustomerNotFoundException;
import ryver.app.customer.CustomerMismatchException;

import ryver.app.account.Account;
import ryver.app.account.AccountRepository;
import ryver.app.account.AccountNotFoundException;
import ryver.app.account.AccountMismatchException;

import ryver.app.transaction.InsufficientBalanceException;

import ryver.app.stock.CustomStock;
import ryver.app.stock.StockRepository;
import ryver.app.stock.InvalidStockException;

import ryver.app.asset.Asset;
import ryver.app.asset.AssetCodeNotFoundException;
import ryver.app.asset.AssetController;
import ryver.app.asset.AssetRepository;

import ryver.app.portfolio.Portfolio;
import ryver.app.portfolio.PortfolioController;
import ryver.app.portfolio.PortfolioNotFoundException;
import ryver.app.portfolio.PortfolioRepository;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class TradeController {
    private TradeRepository trades;
    private CustomerRepository customers;
    private AccountRepository accounts;
    private StockRepository stocks;
    private PortfolioRepository portfolios;
    private PortfolioController portfolioCtrl;
    private AssetRepository assets;
    private AssetController assetCtrl;
   
    public TradeController(TradeRepository trades, CustomerRepository customers, AccountRepository accounts, StockRepository stocks, PortfolioRepository portfolios, PortfolioController portfolioCtrl, AssetRepository assets, AssetController assetCtrl){
        this.trades = trades;
        this.customers = customers;
        this.accounts = accounts;
        this.stocks = stocks;
        this.portfolios = portfolios;
        this.assets = assets;
        this.portfolioCtrl = portfolioCtrl;
        this.assetCtrl = assetCtrl;
        
    }

    /**
     * Get all Trades associated with the logged in Customer's ID Returns 200 OK (if
     * no exceptions)
     * 
     * @return List<Trade>
     */
    @GetMapping("/api/trades")
    public List<Trade> getAllTrades(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        //trades will be returned based on the user that was authenticated
        Customer customer = customers.findByUsername(customerUsername)
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        checkCustomerActive(customer);

        long customerId = customer.getId();

        return trades.findByCustomerId(customerId);

    }

    private void checkCustomerActive(Customer customer) {
        if (!customer.isActive()) {
            throw new InactiveCustomerException();
        }
    }

    /**
     * Get specific Trade associated with the logged in Customer's ID that has the
     * specified TradeId Returns 200 OK (if no exceptions)
     * 
     * @param tradeId
     * @return Trade
     */
    @GetMapping("/api/trades/{tradeId}")
    public Trade getSpecificTrade(@PathVariable (value = "tradeId") Long tradeId){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        //trade will be returned based on the user that was authenticated
        Customer customer = customers.findByUsername(customerUsername)
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        checkCustomerActive(customer);

        long customerId = customer.getId();

        Trade trade = trades.findByIdAndCustomerId(tradeId, customerId)
            .orElseThrow(() -> new TradeNotFoundException(tradeId));

        // if current time exceeds 5pm, update all trade status to expire
        updateStatusToExpire();
        return trade;

    }

    /**
     * Get all open or partial filled buy Trades associated with the specified
     * symbol
     * 
     * @param symbol
     * @return List<Trade>
     */
    public List<Trade> getSpecificStockOpenAndPartialFilledBuyTrade(String symbol) {
        List<Trade> tradeOpen = trades.findByActionAndStatusAndSymbol("buy", "open", symbol);
        List<Trade> tradePartialFilled = trades.findByActionAndStatusAndSymbol("buy", "partial-filled", symbol);
        List<Trade> trade = Stream.concat(tradeOpen.stream(), tradePartialFilled.stream()).collect(Collectors.toList());
        return trade;
    }
     * @param symbol
     * @return List<Trade>
     */
    public List<Trade> getSpecificStockOpenAndPartialFilledSellTrade(String symbol) {
        List<Trade> tradeOpen = trades.findByActionAndStatusAndSymbol("sell", "open", symbol);
        List<Trade> tradePartialFilled = trades.findByActionAndStatusAndSymbol("sell", "partial-filled", symbol);
        List<Trade> trade = Stream.concat(tradeOpen.stream(), tradePartialFilled.stream()).collect(Collectors.toList());
        return trade;

    /**
     * Update the specific Stock ask/bid and ask/bid volume if the Trade made is the
     * best price
     * 
     * @param trade
     * @param stock
     */
    public void updateTradeToStock(Trade trade, CustomStock stock) {
        // if buy order
        if (trade.getSymbol().equals("buy")) {
            // if bid volume is 0 --- means no sell orders yet so no matching
            // check if this trade's bid is higher than the stock's previous bid
            // -> if yes: save new bid price and quantity into the stocks database
            if (stock.getBid_volume() == 0 || trade.getBid() > stock.getBid()) {
                stock.setBid(trade.getBid());
                stock.setBid_volume(trade.getQuantity());
                stocks.save(stock);
            }
            // else do nothing
            // if sell order
        } else {
            // if ask volume is 0 --- means no buy orders yet so no matching
            // if this trade's ask is lower than the stock's previous ask
            // -> save new ask price and quantity into the stocks database
            if (stock.getAsk_volume() == 0 || trade.getAsk() < stock.getAsk()) {
                stock.setAsk(trade.getAsk());
                stock.setAsk_volume(trade.getQuantity());
                stocks.save(stock);
            }
            // else do nothing
        }
        
    }

    /**
     * Update the Trade to expire if the Trade is open or partial filled and if the
     * current time exceeds 5pm on the same day
     */
    // second, minute, hour, day of month, month, day(s) of week
    @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "GMT+8")
    public void updateStatusToExpire() {
        ZonedDateTime current = ZonedDateTime.now();
        int currentHour = current.getHour();
        int fivePM = 17;
        
        List<Trade> allTradeList = trades.findAll();
        for (Trade trade : allTradeList) {
            // date with the 0 value is for the 20k inital stocks
            if (trade.getDate() != 0
                    && (trade.getStatus().equals("open") || trade.getStatus().equals("partial-filled"))) {
                trade.setStatus("expired");
            }

            long accountId = trade.getAccountId();
        
            Account account = accounts.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
            // calculate price according to the stocks that did not get filled
            if (trade.getAction().equals("buy")) {
                double price = (trade.getQuantity() - trade.getFilled_quantity()) * trade.getBid();

                account.setBalance(account.getBalance() + price);
                account.setAvailable_balance(account.getAvailable_balance() + price);
            }

            trades.save(trade);
        }

    }

    // /**
    //  * For buy orders Check if there is a sell trade that matches the current buy
    //  * trade
    //  * 
    //  * @param stock
    //  * @param trade
    //  * @param account
    //  * @param customer
    //  * @param calculatedBuyPrice
    //  */
    // public void buyTradeCheckForSellMatch(CustomStock stock, Trade trade, Account account, Customer customer,
    //         int maxQuantity) {
    //     double stockAsk = stock.getAsk();

    //     double bid = trade.getBid();
    //     if (bid == 0.0) {
    //         bid = stock.getAsk();
    //     }

    //     // if trade's bid is higher than stock's ask -> match & buy
    //     // especially for limit order where trade bid can be lower than stock ask (no
    //     // match)
    //     if (bid >= stockAsk) {
    //         // get list of sell trades available in the market
    //         List<Trade> tradeSellListOfSymbol = getSpecificStockOpenAndPartialFilledSellTrade(trade.getSymbol());

    //         // sort the trade list according to ask and date (lowest ask and lowest date)
    //         Comparator<Trade> compareByAsk = Comparator.comparing(Trade::getAsk);
    //         Comparator<Trade> compareByDate = Comparator.comparing(Trade::getDate);
    //         Comparator<Trade> compareByAskAndDate = compareByAsk.thenComparing(compareByDate);
    //         Collections.sort(tradeSellListOfSymbol, compareByAskAndDate);

    //         int tradeQuantity = trade.getQuantity();
    //         checkTradeQuantityAgainstStockAskVol(stock, trade, account, customer, tradeSellListOfSymbol, tradeQuantity);

    //     } else {
    //         // if trade's bid is lower than stock's ask -> not matched (for limit order)
    //         trade.setStatus("open");
    //         updateTradeToStock(trade, stock);

    //     }
    // }

    /**
     * For buy orders Check if the Stock's ask volume is sufficient to fill the
     * Trade's buy quantity
     * 
     * @param stock
     * @param trade
     * @param account
     * @param customer
     * @param tradeSellListOfSymbol
     * @param tradeQuantity
     */
    public void checkTradeQuantityAgainstStockAskVol(CustomStock stock, Trade trade, Account account, Customer customer,
            int tradeQuantity) {
        double STOCK_ASK = stock.getAsk();
        int STOCK_ASK_VOL = stock.getAsk_volume();

        int filledQuantity = trade.getFilled_quantity(); // already filled
        int unfilledQuantity = tradeQuantity - filledQuantity;

        double available_balance = account.getAvailable_balance();
        double balance = account.getBalance();
        Portfolio portfolio = customer.getPortfolio();
        Trade soldTrade = tradeSellListOfSymbol.get(0);
        Account sellerAccount = soldTrade.getAccount();

        if (unfilledQuantity == 0){
            return;
        }

        // if stock's volume is enough to fill trade's quantity
        if (unfilledQuantity <= STOCK_ASK_VOL) {

            // customer can buy all the trade at that ask price
            trade.setStatus("filled");
            updateAvgPriceAndFilledQtyInTrade(trade, STOCK_ASK, unfilledQuantity);

            // match is done and money is deducted from buyer's account
            deductMoneyFromAccount(account, (STOCK_ASK * unfilledQuantity));

            // update the buyer's portfolio with the newly bought assets
            createAsset(stock, trade, portfolio);

            // update the seller side
            long tradeSellCustomerId = tradeSellListOfSymbol.get(0).getCustomerId();
            Portfolio tradeSellPortfolio = portfolios.findByCustomerId(tradeSellCustomerId)
                .orElseThrow(() -> new PortfolioNotFoundException(tradeSellCustomerId));

            // if the stock fills the trade just nice, revert the stock back to the previous
            // stock
            updateAvgPriceAndFilledQtyInTrade(soldTrade, STOCK_ASK, unfilledQuantity);
            if (soldTrade.getQuantity() == soldTrade.getFilled_quantity()) {
                soldTrade.setStatus("filled");
                // remove that trade from the list if its filled
                tradeSellListOfSymbol.remove(0);

                // if list is empty, set volume to 0
                // else, set as next best trade
                if (tradeSellListOfSymbol.isEmpty()) {
                    stock.setAsk_volume(0);
                } else {
                    stock.setAsk(tradeSellListOfSymbol.get(0).getAsk());
                    stock.setAsk_volume(tradeSellListOfSymbol.get(0).getQuantity());
                }
            } else {
                soldTrade.setStatus("partial-filled");
                // if there's still quantity leftover in stock
                stock.setAsk_volume(STOCK_ASK_VOL - unfilledQuantity);
            }

            // update portfolio
            deleteAsset(stock, soldTrade, tradeSellPortfolio);
            // update seller account
            addMoneyToAccount(sellerAccount, (STOCK_ASK * unfilledQuantity));

        } else { // if the quantity in the stocks is not enough to fill the trade at first (need
                 // recursion)

            // update soldTrade and buyTrade info (avgPrice and filledQuantity)
            updateAvgPriceAndFilledQtyInTrade(soldTrade, STOCK_ASK, STOCK_ASK_VOL);
            soldTrade.setStatus("filled");
            updateAvgPriceAndFilledQtyInTrade(trade, STOCK_ASK, STOCK_ASK_VOL);

            // update the seller's portfolio because his asset has all been sold
            // successfully
            long tradeSellCustomerId = tradeSellListOfSymbol.get(0).getCustomerId();
            Portfolio tradeSellPortfolio = portfolios.findByCustomerId(tradeSellCustomerId)
                    .orElseThrow(() -> new PortfolioNotFoundException(tradeSellCustomerId));
            deleteAsset(stock, tradeSellListOfSymbol.get(0), tradeSellPortfolio);
            // update seller account
            addMoneyToAccount(sellerAccount, (STOCK_ASK * STOCK_ASK_VOL));

            // remove that trade from the list if its filled
            tradeSellListOfSymbol.remove(0);

            // if list is empty, no need to check anymore
            if (tradeSellListOfSymbol.isEmpty()) {
                // set trade
                trade.setStatus("partial-filled"); // buy order is partial filled but all matched sell order is filled
                updateAvgPriceAndFilledQtyInTrade(trade, STOCK_ASK, STOCK_ASK_VOL);

                createAsset(stock, trade, portfolio);

                // update buyers account for whatever stocks bought
                // available balance - stock bought - leftover open stocks
                deductMoneyFromAccount(account, STOCK_ASK * STOCK_ASK_VOL);
                
                // account.setAvailable_balance(available_balance - (STOCK_ASK * STOCK_ASK_VOL)
                //         - (STOCK_ASK * (tradeQuantity - STOCK_ASK_VOL)));
                // account.setBalance(balance - (STOCK_ASK * STOCK_ASK_VOL));

                // set stock
                stock.setAsk(0);
                stock.setAsk_volume(0);
                return;
            }

            // loop the trade sell list of the same symbol
            // to see if there's any other trade with the same ask price
            // if yes, then fill the current trade also
            // if no, change the stock information to the next higher ask price
            for (int j = 0; j < tradeSellListOfSymbol.size(); j++) {
                // set stock
                stock.setAsk(tradeSellListOfSymbol.get(j).getAsk());
                stock.setAsk_volume(tradeSellListOfSymbol.get(j).getQuantity());

                // market order
                if (trade.getBid() == 0.0) {
                    if (STOCK_ASK == tradeSellListOfSymbol.get(j).getAsk()) {
                        tradeQuantity -= STOCK_ASK_VOL;
                        // check for quantity. //if same price
                        checkTradeQuantityAgainstStockAskVol(stock, trade, account, customer, tradeSellListOfSymbol,
                                tradeQuantity);
                    } else {
                        // if the current stock's ask price is not the same as the next ask price then
                        // break

                        // set trade
                        trade.setStatus("partial-filled");
                        trade.setAvg_price(STOCK_ASK);
                        trade.setFilled_quantity(STOCK_ASK_VOL);

                        createAsset(stock, trade, portfolio);

                        // set account
                        // available balance - stock bought - leftover open stocks
                        account.setAvailable_balance(available_balance - (STOCK_ASK * STOCK_ASK_VOL)
                                - (STOCK_ASK * (tradeQuantity - STOCK_ASK_VOL)));
                        account.setBalance(balance - (STOCK_ASK * STOCK_ASK_VOL));

                        break;
                    }
                } else {
                    // limit order
                    if (trade.getBid() >= tradeSellListOfSymbol.get(j).getAsk()) {
                        tradeQuantity -= STOCK_ASK_VOL;
                        // check for quantity.
                        checkTradeQuantityAgainstStockAskVol(stock, trade, account, customer, tradeSellListOfSymbol,
                                tradeQuantity);
                    } else {
                        // if the current stock's ask price is not the less than the next ask price then
                        // break -- no more matching, remain partial filled

                        // set trade
                        trade.setStatus("partial-filled");
                        trade.setAvg_price(STOCK_ASK);
                        trade.setFilled_quantity(STOCK_ASK_VOL);

                        createAsset(stock, trade, portfolio);

                        // set account
                        // available balance - stock bought - leftover open stocks
                        account.setAvailable_balance(available_balance - (STOCK_ASK * STOCK_ASK_VOL)
                                - (STOCK_ASK * (tradeQuantity - STOCK_ASK_VOL)));
                        account.setBalance(balance - (STOCK_ASK * STOCK_ASK_VOL));

                        break;
                    }
                }
            }

        }
    }

    private void deductMoneyFromAccount(Account account, double amount) {
        account.setAvailable_balance(account.getAvailable_balance() - amount);
        account.setBalance(account.getBalance() - amount);
    }

    private void addMoneyToAccount(Account account, double amount) {
        account.setAvailable_balance(account.getAvailable_balance() + amount);
        account.setBalance(account.getBalance() + amount);
    }

    private void updateAvgPriceAndFilledQtyInTrade(Trade trade, double newPrice, int newQty) {
        double previousPrice = trade.getAvg_price() * trade.getFilled_quantity();
        trade.setFilled_quantity(trade.getFilled_quantity() + newQty);
        double newAvgPrice = (previousPrice + (newPrice * newQty)) / trade.getFilled_quantity();
        trade.setAvg_price(newAvgPrice);
    }

    /**
     * Create an Asset if the buy order gets filled
     * 
     * @param stock
     * @param trade
     * @param portfolio
     */
    private void createAsset(CustomStock stock, Trade trade, Portfolio portfolio) {
        // for buying
        if (trade.getAction().equals("buy")) {
            double bid = trade.getBid();
            if (bid == 0.0) {
                bid = stock.getAsk();
            }
            long portfolioId = portfolio.getId();
            String code = trade.getSymbol();   
            
            Optional<Asset> nothing = Optional.empty();
            // asset already exist in portfolio -> update asset
            if (assets.findByCodeAndPortfolioId(code, portfolioId) != nothing) {
                Asset asset = assets.findByCodeAndPortfolioId(code, portfolioId)
                    .orElseThrow(() -> new AssetCodeNotFoundException(code));

                long assetId = asset.getId();
                int prevQuantity = asset.getQuantity();
                double prevAvg_price = asset.getAvg_price();
                double prevTotalPrice = prevQuantity * prevAvg_price;
                
                int newQuantity = prevQuantity + trade.getFilled_quantity();
                double newTotalPrice = prevTotalPrice + (trade.getFilled_quantity() * bid);
                double newAvg_price = newTotalPrice / newQuantity;
            
                Asset newAsset = asset;
                newAsset.setQuantity(newQuantity);
                newAsset.setAvg_price(newAvg_price);
                assetCtrl.updateAsset(portfolioId, assetId, newAsset);
            } else {
                // asset does not exist in portfolio -> add trade to asset
                int quantity = trade.getFilled_quantity();
                double avg_price = bid;
                double current_price = stock.getBid();
                double value = current_price * quantity;
                double gain_loss = value - (avg_price * quantity);
            
                Asset asset = new Asset(code, quantity, avg_price, current_price, value, gain_loss);
                asset.setPortfolio(portfolio);
                assetCtrl.addAsset(portfolioId, asset);
            }
            // update unrealized gain loss in portfolio
            Portfolio updatedPortfolio = portfolio;
            portfolioCtrl.updatePortfolio(portfolioId, updatedPortfolio);
        }
    }

    private void deleteAsset(CustomStock stock, Trade trade, Portfolio portfolio) {
        // for selling
        if (trade.getAction().equals("sell")) {
            double ask = trade.getAsk();
            if (ask == 0.0) {
                ask = stock.getBid();
            }

            long portfolioId = portfolio.getId();
            String code = trade.getSymbol();
        
            Asset asset = assets.findByCodeAndPortfolioId(code, portfolioId)
                .orElseThrow(() -> new AssetCodeNotFoundException(code));

            long assetId = asset.getId();

            int prevQuantity = asset.getQuantity();
            int newQuantity = prevQuantity - trade.getFilled_quantity();

            // if trade quantity < asset quantity -> minus
            if (trade.getQuantity() < asset.getQuantity()) {
                

                Asset newAsset = asset;
                newAsset.setQuantity(newQuantity);
                
                assetCtrl.updateAsset(portfolioId, assetId, newAsset);
                
            } else {
                // if trade quantity == asset quantity -> delete
                assetCtrl.deleteAsset(portfolioId, assetId);
            }

            // update total gain loss in portfolio
            double prevTotalGainLoss = portfolio.getTotal_gain_loss();
            double thisTotalGainLoss = trade.getFilled_quantity() * (ask - asset.getAvg_price());
            double newTotalGainLoss = prevTotalGainLoss + thisTotalGainLoss;

            Portfolio updatedPortfolio = portfolio;
            updatedPortfolio.setTotal_gain_loss(newTotalGainLoss);
            portfolioCtrl.updatePortfolio(portfolioId, updatedPortfolio);
        }   
    }

    /**
     * For sell orders Check if there is a buy trade that matches the current sell
     * trade
     * 
     * @param stock
     * @param trade
     * @param account
     * @param customer
     */
    public void sellTradeCheckForBuyMatch(CustomStock stock, Trade trade, Account account, Customer customer) {
        double stockBid = stock.getBid();

        double ask = trade.getAsk();

        if (ask == 0.0) {
            ask = stockBid;
        }
        int quantity = trade.getQuantity();

        // if trade's ask is lower than stock's bid -> match & sell
        if (ask <= stockBid) {
            // if trade was not previously filled
            if (trade.getAvg_price() == 0.0) {
                trade.setAvg_price(stockBid);
            } else {
                // if trade was previously filled
                double previousPrice = trade.getAvg_price() * trade.getFilled_quantity();
                double newAvgPrice = previousPrice + (stockBid * quantity);
                trade.setAvg_price(newAvgPrice);
                trade.setFilled_quantity(trade.getFilled_quantity() + quantity);
            }

            List<Trade> tradeBuyListOfSymbol = stock.getTrades();

            // remove the sell and filled trades
            Iterator<Trade> i = tradeBuyListOfSymbol.iterator();
            while (i.hasNext()) {
                Trade t = i.next();
                if (t.getAction().equals("sell") || t.getStatus().equals("filled")) {
                    i.remove();
                }
            }
        
            if (tradeBuyListOfSymbol.isEmpty()) {
                stock.setBid(0);
                stock.setBid_volume(0);

                // no more selling stock in the market 
                // throw new EmptySellingStockInMarket(); 
                return;
            }
        
            Comparator<Trade> compareByBid = Comparator.comparing( Trade::getBid );
            Comparator<Trade> compareByDate = Comparator.comparing( Trade::getDate );
            Comparator<Trade> compareByBidAndDate = compareByBid.thenComparing(compareByDate);
            
            Collections.sort(tradeBuyListOfSymbol, compareByBidAndDate);
        
            int tradeQuantity = quantity;
            checkTradeQuantityAgainstStockBidVol(stock, trade, account, customer, tradeBuyListOfSymbol, tradeQuantity);
        } else {
            // if trade's ask is higher than stock's bid -> not matched
            trade.setStatus("open");
            updateTradeToStock(trade, stock);
        }
    }

    /**
     * For sell orders Check if the Stock's bid volume is sufficient to fill the
     * Trade's sell quantity
     * 
     * @param stock
     * @param trade
     * @param account
     * @param customer
     * @param tradeSellListOfSymbol
     * @param tradeQuantity
     */
    public void checkTradeQuantityAgainstStockBidVol(CustomStock stock, Trade trade, Account account, Customer customer,
            List<Trade> tradeBuyListOfSymbol, int tradeQuantity) {
        double stockBid = stock.getBid();
        int stockBidVol = (int)stock.getBid_volume();

        // double bid = trade.getBid();
        // int quantity = trade.getQuantity();

        int filledQuantity = trade.getFilled_quantity();

        double available_balance = account.getAvailable_balance();
        double balance = account.getBalance();
        
        // if stock's volume is enough to fill trade's quantity
        if (tradeQuantity <= (stockBidVol - filledQuantity)) {
            trade.setStatus("filled");
            trade.setFilled_quantity(trade.getQuantity());
            trade.setAvg_price(stockBid);
        
            account.setAvailable_balance(available_balance + (stockBid * tradeQuantity));
            account.setBalance(balance + (stockBid * tradeQuantity));
            
            // delete entry from portfolio
            Portfolio portfolio = customer.getPortfolio();
            deleteAsset(stock, trade, portfolio);

            long tradeBuyCustomerId = tradeBuyListOfSymbol.get(0).getCustomerId();
            Portfolio tradeBuyPortfolio = portfolios.findByCustomerId(tradeBuyCustomerId)
                .orElseThrow(() -> new PortfolioNotFoundException(tradeBuyCustomerId));

            createAsset(stock, tradeBuyListOfSymbol.get(0), tradeBuyPortfolio);
        
            // if the stock fills the trade just nice, revert the stock back to the previous stock
            if (tradeQuantity == stockBidVol) {
                tradeBuyListOfSymbol.get(0).setStatus("filled");
                trade.setFilled_quantity(tradeBuyListOfSymbol.get(0).getQuantity());
                // remove that trade from the list if its filled
                tradeBuyListOfSymbol.remove(0);
                
                double newStockBid = tradeBuyListOfSymbol.get(0).getBid();
                int newStockBidVol = tradeBuyListOfSymbol.get(0).getQuantity();
                stock.setBid(newStockBid);
                stock.setBid_volume(newStockBidVol);
            
            } else {
                // if there's still quantity leftover in stock
                stock.setBid_volume(stockBidVol - tradeQuantity);
                // minus from assets

                tradeBuyListOfSymbol.get(0).setStatus("partial-filled");
                tradeBuyListOfSymbol.get(0).setFilled_quantity(tradeQuantity);
                tradeBuyListOfSymbol.get(0).setAvg_price(stockBid);
                
            }
        } else {
            // if the quantity in the stocks is not enough to fill the trade

            // set the current best trade (stock) to filled
            // remove the sell and filled trades
            Iterator<Trade> i = tradeBuyListOfSymbol.iterator();
            while (i.hasNext()) {
                Trade t = i.next();
                if (t.getAction().equals("sell") || t.getStatus().equals("filled")) {
                    i.remove();
                }
            }

            Portfolio portfolio = customer.getPortfolio();

            tradeBuyListOfSymbol.get(0).setStatus("filled");
            trade.setFilled_quantity(tradeBuyListOfSymbol.get(0).getQuantity());

            long tradeBuyCustomerId = tradeBuyListOfSymbol.get(0).getCustomerId();
            Portfolio tradeBuyPortfolio = portfolios.findByCustomerId(tradeBuyCustomerId)
                .orElseThrow(() -> new PortfolioNotFoundException(tradeBuyCustomerId));

            createAsset(stock, tradeBuyListOfSymbol.get(0), tradeBuyPortfolio);
            // remove that trade from the list if its filled
            tradeBuyListOfSymbol.remove(0);
        
            // if list is not empty
            if (!(tradeBuyListOfSymbol.isEmpty())) {
                // loop the trade sell list of the same symbol
                // to see if there's any other trade with the same bid price
                // if yes, then fill the current trade also
                // if no, change the stock information to the next higher bid price
                for (int j = 0; j < tradeBuyListOfSymbol.size(); j++) {
                    // set stock
                    double newStockBid = tradeBuyListOfSymbol.get(j).getBid();
                    int newStockBidVol = (int)tradeBuyListOfSymbol.get(j).getQuantity();
                    stock.setBid(newStockBid);
                    stock.setBid_volume(newStockBidVol);


                    if (stockBid == tradeBuyListOfSymbol.get(j).getBid()) {
                        tradeQuantity -= stockBidVol; 
                        // check for quantity.
                        checkTradeQuantityAgainstStockBidVol(stock, trade, account, customer, tradeBuyListOfSymbol, tradeQuantity);
                    } else {
                        // if the current stock's bid price is not the same as the next bid price then break
                        // set trade
                        trade.setStatus("partial-filled");
                        trade.setAvg_price(stockBid);
                        trade.setFilled_quantity(stockBidVol);

                        deleteAsset(stock, trade, portfolio);

                        // set account
                        // balance + stock sold
                        account.setAvailable_balance(available_balance + (stockBid * stockBidVol));
                        account.setBalance(balance + (stockBid * stockBidVol));

                        break;
                    }
                }
            } else {
                // set trade
                trade.setStatus("partial-filled");
                trade.setAvg_price(stockBid);
                trade.setFilled_quantity(stockBidVol);

                deleteAsset(stock, trade, portfolio);
                // set account
                // available balance + stock sold
                account.setAvailable_balance(available_balance + (stockBid * stockBidVol));
                account.setBalance(balance + (stockBid * stockBidVol));

                // // set stock
                // // no more stock in the market
                // stock.setAsk_volume(0);

                // no more selling stock in the market 
                // throw new EmptySellingStockInMarket(); 
            }
        }
    }

    /**
     * Create a new Trade using the JSON data Returns 201 Created (if no exceptions)
     * 
     * @param trade
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/trades")
    public Trade createTrade(@Valid @RequestBody Trade trade){
        // check if logged in user == customerId in trade
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Customer customer = customers.findByUsername(customerUsername)
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        checkCustomerActive(customer);

        long loggedInCustomerId = customer.getId();

        if (loggedInCustomerId != trade.getCustomerId()) {
            throw new CustomerMismatchException();
        }

        // check if account belongs to customer
        long accountId = trade.getAccountId();
        // 1. check if account exists
        accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        // 2. if account exists, check if customer owns the account
        Account account = accounts.findByIdAndCustomerId(accountId, loggedInCustomerId)
                .orElseThrow(() -> new AccountMismatchException());

        // check if quantity % 100 == 0 (if not bad request)
        int quantity = trade.getQuantity();
        if (quantity % 100 != 0) {
            throw new InvalidQuantityException();
        }

        // check if action has the correct input
        String action = trade.getAction();
        if (!(action.equals("buy") || action.equals("sell"))) {
            throw new InvalidTradeException();
        }

        double bid = trade.getBid();
        double ask = trade.getAsk();
        double calculatedBuyPrice;

        // check if symbol exist in database
        String symbol = trade.getSymbol();
        CustomStock stock = stocks.findBySymbol(symbol).orElseThrow(() -> new InvalidStockException(symbol));

            // if customer does not have a portfolio
        Portfolio portfolio = portfolios.findByCustomerId(customer.getId()) 
        .orElseThrow(() -> new PortfolioNotFoundException(customer.getId()));

        // check current time
        ZonedDateTime current = ZonedDateTime.now();
        int currentHour = current.getHour();
        final int NINE_AM = 9;
        final int FIVE_PM = 17;
        String currentDay = current.getDayOfWeek().name();

        // if day is saturday or sunday
        // if hour is before 9am and after 5pm
        // then trade not matched
        // if ((currentDay.equals("SATURDAY") || currentDay.equals("SUNDAY"))
        // || (currentHour < NINE_AM || currentHour >= FIVE_PM)) {
        // trade.setAccount(account);
        // trade.setStock(stock);
        // trade.setPortfolio(portfolio);
        // trade.setStatus("open");
        // updateTradeToStock(trade, stock);
        // return trades.save(trade);
        // }
        //let trades be open when they are first created, to be changed after matching later
        trade.setStatus("open");
        if (action.equals("buy")) {
            buying(trade, customer, account, quantity, bid, symbol, stock);
        } else {
            selling(trade, customer, account, ask, symbol, stock, portfolio);
        }

        // set current timestamp to date
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        trade.setDate(timestamp.getTime());

        trade.setAccount(account);
        trade.setStock(stock);

        accounts.save(account);
        return trades.save(trade);
    }

    private void selling(Trade trade, Customer customer, Account account, double ask, String symbol, CustomStock stock,
            Portfolio portfolio) {
        // FOR SELLING
        double stockBid = stock.getBid();

        // if it's a sell market order, change the ask value to current stock's bid
        // price
        if (ask == 0.0) {
            ask = stockBid;
        }
        long portfolioId = portfolio.getId();

        // if customer does not have the same stock in portofolio
        Asset asset = assets.findByCodeAndPortfolioId(symbol, portfolioId)
                .orElseThrow(() -> new InvalidStockException(symbol));

        // if quantity in portfolio is lesser than trade quantity, then throw exception
        if (trade.getQuantity() > asset.getQuantity()) {
            throw new InsufficientStockException();
        }

        List<Trade> specificStockOpenAndPartialFilledBuyTrade = getSpecificStockOpenAndPartialFilledBuyTrade(symbol);

        // check if the market has the buying stocks the customer is selling
        if (!(specificStockOpenAndPartialFilledBuyTrade.isEmpty())) {
            // check price in previous trades (better price match first)
            sellTradeCheckForBuyMatch(stock, trade, account, customer);
        } else {
            // if stocks not matched and the customer's trade is the best price
            // then update the stock with the customer's trade
            trade.setStatus("open");
            updateTradeToStock(trade, stock);
        }
    }

    private void buying(Trade trade, Customer customer, Account account, int quantity, double bid, String symbol,
            CustomStock stock) {
        // FOR BUYING
        // quantity = buyer trade quantity
                
        //if it is a limit order:
        if (bid != 0.0) {
            //do basic check of whether buyer can buy the qty he specify, else, ecxception will be thrown
            double amountNeeded = bid * quantity;
            if (account.getAvailable_balance() < amountNeeded)
                throw new InsufficientBalanceException();

        }

        // double calculatedBuyPrice = bid * quantity;

        // if (maxQuantity > stock.getAsk_volume()) {
        //     maxQuantity = stock.getAsk_volume(); // maxQuantity is limited by how much is sold in the market
        // } // definitely enough money to buy

        // // if it's a buy market order or bid price is higher than stock's ask price
        // // change the bid value to current stock's ask price
        // if (bid == 0.0 || bid > stock.getAsk()) {
        //     bid = stock.getAsk();
        // }

        // double available_balance = account.getAvailable_balance();
        // double fundsOnHold = maxQuantity * bid;

        List<Trade> specificStockOpenAndPartialFilledSellTrade = getSpecificStockOpenAndPartialFilledSellTrade(symbol);
        // sort the trade list according to ask and date (lowest ask and lowest date)
        Comparator<Trade> compareByAsk = Comparator.comparing(Trade::getAsk);
        Comparator<Trade> compareByDate = Comparator.comparing(Trade::getDate);
        Comparator<Trade> compareByAskAndDate = compareByAsk.thenComparing(compareByDate);
        Collections.sort(specificStockOpenAndPartialFilledSellTrade, compareByAskAndDate);

        // check if the market has the stocks the customer is buying
        if (specificStockOpenAndPartialFilledSellTrade.isEmpty()) {
            // check if stocks not matched and the customer's trade is the best price
            // then update the stock with the customer's trade
            updateTradeToStock(trade, stock);

        } else {
            // check if there's sufficient balance, update available balance to hold money needed
            // for trading

            // account.setAvailable_balance(available_balance - fundsOnHold);

            // do the matching
            // check other prices in previous trades too, in case ask_volume not enough to
            // fill quantity (better price match first)
            for (Trade sellTrade : specificStockOpenAndPartialFilledSellTrade) {

                // for each sellTrade.askPrice, check the max quantity customer can buy based on funds
                int maxQuantity = getMaxStocksAbleToBuy(account, trade, sellTrade);

                //when trade is commpletely filled
                if (trade.getFilled_quantity() == trade.getQuantity()){
                    break;
                }
                if (bid >= sellTrade.getAsk() && maxQuantity > 0){
                    //do matching
                    matchWithSellTrade(stock, trade, sellTrade, maxQuantity);
                    // checkTradeQuantityAgainstStockAskVol(stock, trade, account, customer, tradeQuantity);

                } else if (bid < sellTrade.getAsk()){
                    // if bid is lower than stockAsk, no point in going through the rest of the list
                    updateTradeToStock(trade, stock);
                    return;
                }
            }
        }
    }

    private void matchWithSellTrade (CustomStock stock, Trade buyTrade, Trade sellTrade, int maxQuantity){
        /* 
         * UPON successful matching, 
         * 1. increase filledQuantity in buyTrade by maxQuantity
         * 1a. if filled, updated status to "filled"
         * 2. deduct from buyer's account
         * 3. update buyer's portfolio (increase/add Asset)
         * 
         * 4. increase filledQuantity in sellTrade by maxQuantity
         * 4a. if filled, update status to "filled"
         * 5. add to seller's acocunt
         * 6. update seller's portfolio (decrease/delete Asset)
         * 
         * 7. updateStockInfo -- ask_Price, ask_vol
        */

        buyTrade.setFilled_quantity(buyTrade.getFilled_quantity() + maxQuantity);
        checkIfFilledOrPartialFilled(buyTrade);

        double pay = maxQuantity * sellTrade.getAsk();
        deductMoneyFromAccount(buyTrade.getAccount(), pay);

        Portfolio buyerPortfolio = portfolios.findByCustomerId(buyTrade.getCustomerId())
            .orElseThrow(() -> new PortfolioNotFoundException(buyTrade.getCustomerId()));
        createAsset(stock, buyTrade, buyerPortfolio);
        
        sellTrade.setFilled_quantity(sellTrade.getFilled_quantity() + maxQuantity);



    }

    private void checkIfFilledOrPartialFilled (Trade trade){
        if (trade.getFilled_quantity() == trade.getQuantity()){
            trade.setStatus("filled");
        } else if (trade.getFilled_quantity() > 0 && trade.getFilled_quantity() < trade.getQuantity()){
            trade.setStatus("partial-filled");
        }
    }
    private void updateStockInfo (CustomStock stock, Trade trade){ // to update ask_Price, ask_vol
    }

    private int getMaxStocksAbleToBuy(Account account, Trade buyTrade, Trade sellTrade) {
        // check if account has sufficient balance(for the particular bid price)
        double available_balance = account.getAvailable_balance();

        double priceOfStock = sellTrade.getAsk();

        int sellQuantity = sellTrade.getQuantity() - sellTrade.getFilled_quantity();
        int buyQuantity = buyTrade.getQuantity();

        // market/limit order:
        // check how much buyer can buy based on balance and price
        int maxQuantity = ((int) (available_balance / priceOfStock) / 100) * 100;

        if (buyQuantity > sellQuantity && maxQuantity > sellQuantity){
            //when you have enough amount of money and try to buy more than the sellQuantity in sellTrade,
            // the max you can buy per sellTrade is bounded by the sellQuantity of the sellTrade
            return sellQuantity;
        } else if (buyQuantity < sellQuantity && maxQuantity > sellQuantity){
            //when you have enough amount of money to fill all the sellQuantity but you don't want to buy all the sellQuantity in sellTrade, 
            // the max you can buy per sellTrade is bounded by the buyQuantity you specified
            return buyQuantity;
        } else {
            ///when you have limited money, and cannot even buy your buyQuantity, you buy whatever you can 
            return maxQuantity;
        }
        
       

    }

    /**
     * Cancel a specific Trade, based on JSON data Filled or partial filled Trades
     * cannot be cancelled Returns 200 OK (if no exceptions)
     * 
     * @param tradeId
     * @param updatedTradeInfo
     * @return Trade
     */
    @PutMapping("/api/trades/{tradeId}")
    public Trade updateSpecificTrade(@PathVariable (value = "tradeId") Long tradeId, @Valid @RequestBody Trade updatedTradeInfo){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        // user that was authenticated
        Customer customer = customers.findByUsername(customerUsername)
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        checkCustomerActive(customer);

        long customerId = customer.getId();
        
        // check if the trade has been made by the authenticated user before
        Trade trade = trades.findByIdAndCustomerId(tradeId, customerId)
            .orElseThrow(() -> new TradeNotFoundException(tradeId));

        // customer can cancel a trade if its open
        if (trade.getStatus().equals("open") && updatedTradeInfo.getStatus().equals("cancelled")) {
            trade.setStatus("cancelled");
        } else {
            // if trade is already filled or partial filled
            throw new WrongStatusException();
        }

        long accountId = trade.getAccountId();

        Account account = accounts.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));

        // set back the available balance if its a buy order
        if (trade.getAction().equals("buy")) {
            account.setAvailable_balance(account.getBalance());
        }

        trades.save(trade);
        return trade;
    }
}