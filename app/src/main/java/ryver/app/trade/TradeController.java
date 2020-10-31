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

    // customers can get all a list of all their trade
    @GetMapping("/api/trades")
    public List<Trade> getAllTrades(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        //trades will be returned based on the user that was authenticated
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
        long customerId = customer.getId();

        return trades.findByCustomerId(customerId);

    }

    // customers can get their specific trade
    @GetMapping("/api/trades/{tradeId}")
    public Trade getSpecificTrade(@PathVariable (value = "tradeId") Long tradeId){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        //trade will be returned based on the user that was authenticated
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
        long customerId = customer.getId();

        Trade trade = trades.findByIdAndCustomerId(tradeId, customerId)
            .orElseThrow(() -> new TradeNotFoundException(tradeId));

        // if current time exceeds 5pm, update all trade status to expire
        updateStatusToExpire();
        return trade;

    }

    // search for open & partial filled buy trades according to symbol
    public List<Trade> getSpecificStockOpenAndPartialFilledBuyTrade(String symbol){ 
        List<Trade> tradeOpen = trades.findByActionAndStatusAndSymbol("buy", "open", symbol);
        List<Trade> tradePartialFilled = trades.findByActionAndStatusAndSymbol("buy", "partial-filled", symbol);
        List<Trade> trade = Stream.concat(tradeOpen.stream(), tradePartialFilled.stream()).collect(Collectors.toList());
        return trade;
    }
 
    // search for open & partial filled sell trades according to symbol
    public List<Trade> getSpecificStockOpenAndPartialFilledSellTrade(String symbol){
        List<Trade> tradeOpen = trades.findByActionAndStatusAndSymbol("sell", "open", symbol);
        List<Trade> tradePartialFilled = trades.findByActionAndStatusAndSymbol("sell", "partial-filled", symbol);
        List<Trade> trade = Stream.concat(tradeOpen.stream(), tradePartialFilled.stream()).collect(Collectors.toList());
        return trade;
    }

    public void updateTradeToStock(Trade trade, CustomStock stock) {
        if (trade.getSymbol().equals("buy")) {
            // if this trade's bid is lower than the stock's previous ask
            // if this trade's bid is higher than the stock's previous bid
            // -> save new bid price and quantity into the stocks database
            if ((trade.getBid() < stock.getAsk()) && (trade.getBid() > stock.getBid())) {
                stock.setBid(trade.getBid());
                stock.setBid_volume(trade.getQuantity());
                stocks.save(stock);
            }
            
        } else {
            // if this trade's ask is higher than the stock's previous bid
            // if this trade's ask is lower than the stock's previous ask 
            // -> save new ask price and quantity into the stocks database
            if ((trade.getAsk() > stock.getBid()) && (trade.getAsk() < stock.getAsk())) {
                stock.setAsk(trade.getAsk());
                stock.setAsk_volume(trade.getQuantity());
                stocks.save(stock);
            }
        }
        
    }

    // if trade exceeds 5pm & trade is open/partial-filled -> set status to expired
    public void updateStatusToExpire() {
        ZonedDateTime current = ZonedDateTime.now();
        int currentHour = current.getHour();
        int fivePM = 17;
        
        List<Trade> allTradeList = trades.findAll();
        for (Trade trade : allTradeList) {
            // date with the 0 value is for the 20k inital stocks
            if (trade.getDate() != 0 && currentHour >= fivePM && (trade.getStatus().equals("open") || trade.getStatus().equals("partial-filled"))) {
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

    // for buying
    public void buyTradeCheckForSellMatch(CustomStock stock, Trade trade, Account account, Customer customer, double calculatedBuyPrice) {
        double stockAsk = stock.getAsk();

        double bid = trade.getBid();

        if (bid == 0.0) {
            bid = stock.getAsk();
        }
        int quantity = trade.getQuantity();

        double available_balance = account.getAvailable_balance();

        // if trade's bid is higher than stock's ask -> match & buy
        if (bid >= stockAsk) {
            // if trade was not previously filled
            if (trade.getAvg_price() == 0.0) {
                trade.setAvg_price(stockAsk);
            } else {
                // if trade was previously filled
                double previousPrice = trade.getAvg_price() * trade.getFilled_quantity();
                double newAvgPrice = previousPrice + (stockAsk * quantity);
                trade.setAvg_price(newAvgPrice);
                trade.setFilled_quantity(trade.getFilled_quantity() + quantity);
            }

            // sort the trade list according to ask and date (lowest ask and lowest date)
            List<Trade> tradeSellListOfSymbol = stock.getTrades();

            // remove the buy and filled trades
            Iterator<Trade> i = tradeSellListOfSymbol.iterator();
            while (i.hasNext()) {
                Trade t = i.next();
                if (t.getAction().equals("buy") || t.getStatus().equals("filled")) {
                    i.remove();
                }
            }

            if (tradeSellListOfSymbol.isEmpty()) {
                // no more stock in the market 
                stock.setAsk(0);
                stock.setAsk_volume(0);

                return;
            }

            Comparator<Trade> compareByAsk = Comparator.comparing( Trade::getAsk );
            Comparator<Trade> compareByDate = Comparator.comparing( Trade::getDate );
            Comparator<Trade> compareByAskAndDate = compareByAsk.thenComparing(compareByDate);
            
            Collections.sort(tradeSellListOfSymbol, compareByAskAndDate);
            
            int tradeQuantity = quantity;
            checkTradeQuantityAgainstStockAskVol(stock, trade, account, customer, tradeSellListOfSymbol, tradeQuantity);
        } else {
            // if trade's bid is lower than stock's ask -> not matched
            trade.setStatus("open");
            updateTradeToStock(trade, stock);
            // if there's sufficient balance, set available balance to new balance
            account.setAvailable_balance(available_balance - calculatedBuyPrice);
        }
    }

    // for buying
    public void checkTradeQuantityAgainstStockAskVol(CustomStock stock, Trade trade, Account account, Customer customer, List<Trade> tradeSellListOfSymbol, int tradeQuantity) {
        // final double prevStockAsk = stock.getAsk().doubleValue();
        double stockAsk = stock.getAsk();
        int stockAskVol = (int)stock.getAsk_volume();

        // double bid = trade.getBid();
        // int quantity = trade.getQuantity();

        int filledQuantity = trade.getFilled_quantity();

        double available_balance = account.getAvailable_balance();
        double balance = account.getBalance();

        // if stock's volume is enough to fill trade's quantity
        if (tradeQuantity <= (stockAskVol - filledQuantity)) {
            trade.setStatus("filled");
            trade.setFilled_quantity(trade.getQuantity());
            trade.setAvg_price(stockAsk);
            stock.setLast_price(stockAsk);

            account.setAvailable_balance(available_balance - (stockAsk * tradeQuantity));
            account.setBalance(balance - (stockAsk * tradeQuantity));

            Portfolio portfolio = customer.getPortfolio();
            createAsset(stock, trade, portfolio);

            long tradeSellCustomerId = tradeSellListOfSymbol.get(0).getCustomerId();
            Portfolio tradeSellPortfolio = portfolios.findByCustomerId(tradeSellCustomerId)
                .orElseThrow(() -> new PortfolioNotFoundException(tradeSellCustomerId));

            
            // if the stock fills the trade just nice, revert the stock back to the previous stock
            if (tradeQuantity == stockAskVol) {
                tradeSellListOfSymbol.get(0).setStatus("filled");
                tradeSellListOfSymbol.get(0).setFilled_quantity(tradeSellListOfSymbol.get(0).getQuantity());

                deleteAsset(stock, tradeSellListOfSymbol.get(0), tradeSellPortfolio);
                
                // remove that trade from the list if its filled
                tradeSellListOfSymbol.remove(0);
                
                stock.setAsk(tradeSellListOfSymbol.get(0).getAsk());
                stock.setAsk_volume(tradeSellListOfSymbol.get(0).getQuantity());
            
            } else {
                // if there's still quantity leftover in stock
                stock.setAsk_volume(stockAskVol - tradeQuantity);

                tradeSellListOfSymbol.get(0).setStatus("partial-filled");
                tradeSellListOfSymbol.get(0).setFilled_quantity(tradeQuantity);
                tradeSellListOfSymbol.get(0).setAvg_price(stockAsk);
                
                deleteAsset(stock, tradeSellListOfSymbol.get(0), tradeSellPortfolio);
            }
        } else {
            // if the quantity in the stocks is not enough to fill the trade

            // set the current best trade (stock) to filled
            // remove the buy and filled trades
            Iterator<Trade> i = tradeSellListOfSymbol.iterator();
            while (i.hasNext()) {
                Trade t = i.next();
                if (t.getAction().equals("buy") || t.getStatus().equals("filled")) {
                    i.remove();
                }
            }

            Portfolio portfolio = customer.getPortfolio();

            tradeSellListOfSymbol.get(0).setStatus("filled");
            trade.setFilled_quantity(tradeSellListOfSymbol.get(0).getQuantity());

            long tradeSellCustomerId = tradeSellListOfSymbol.get(0).getCustomerId();
            Portfolio tradeSellPortfolio = portfolios.findByCustomerId(tradeSellCustomerId)
                .orElseThrow(() -> new PortfolioNotFoundException(tradeSellCustomerId));

            deleteAsset(stock, tradeSellListOfSymbol.get(0), tradeSellPortfolio);

            // remove that trade from the list if its filled
            tradeSellListOfSymbol.remove(0);
            
            // if list is not empty
            if (!(tradeSellListOfSymbol.isEmpty())) {
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
                        if (stockAsk == tradeSellListOfSymbol.get(j).getAsk()) {
                            tradeQuantity -= stockAskVol; 
                            // check for quantity.
                            checkTradeQuantityAgainstStockAskVol(stock, trade, account, customer, tradeSellListOfSymbol, tradeQuantity);
                        } else {
                            // if the current stock's ask price is not the same as the next ask price then break
            
                            // set trade
                            trade.setStatus("partial-filled");
                            trade.setAvg_price(stockAsk);
                            trade.setFilled_quantity((int)stockAskVol);
    
                            createAsset(stock, trade, portfolio);

                            // set account
                            // available balance - stock bought - leftover open stocks
                            account.setAvailable_balance(available_balance - (stockAsk * stockAskVol) - (stockAsk * (tradeQuantity - stockAskVol)));
                            account.setBalance(balance - (stockAsk * stockAskVol));
            
                            break;
                        }
                    } else {
                        // limit order
                        if (trade.getBid() >= tradeSellListOfSymbol.get(j).getAsk()) {
                            tradeQuantity -= stockAskVol; 
                            // check for quantity.
                            checkTradeQuantityAgainstStockAskVol(stock, trade, account, customer, tradeSellListOfSymbol, tradeQuantity);
                        } else {
                            // if the current stock's ask price is not the same as the next ask price then break
            
                            // set trade
                            trade.setStatus("partial-filled");
                            trade.setAvg_price(stockAsk);
                            trade.setFilled_quantity((int)stockAskVol);
                            
                            createAsset(stock, trade, portfolio);
    
                            // set account
                            // available balance - stock bought - leftover open stocks
                            account.setAvailable_balance(available_balance - (stockAsk * stockAskVol) - (stockAsk * (tradeQuantity - stockAskVol)));
                            account.setBalance(balance - (stockAsk * stockAskVol));
            
                            break;
                        }
                    }


                    
                }
            } else {
                // set trade
                trade.setStatus("partial-filled");
                trade.setAvg_price(stockAsk);
                trade.setFilled_quantity((int)stockAskVol);
                createAsset(stock, trade, portfolio);

                // set account
                // available balance - stock bought - leftover open stocks
                account.setAvailable_balance(available_balance - (stockAsk * stockAskVol) - (stockAsk * (tradeQuantity - stockAskVol)));
                account.setBalance(balance - (stockAsk * stockAskVol));

                // set stock
                // no more stock in the market 
                stock.setAsk(0);
                stock.setAsk_volume(0);
            
            }

            

        }
    }

    

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
    // for selling
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
                // no more stock in the market 
                stock.setBid(0);
                stock.setBid_volume(0);

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

    // for selling
    public void checkTradeQuantityAgainstStockBidVol(CustomStock stock, Trade trade, Account account, Customer customer, List<Trade> tradeBuyListOfSymbol, int tradeQuantity) {
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
                        trade.setFilled_quantity((int)stockBidVol);
            
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
                trade.setFilled_quantity((int)stockBidVol);
                
                deleteAsset(stock, trade, portfolio);
                // set account
                // available balance + stock sold
                account.setAvailable_balance(available_balance + (stockBid * stockBidVol));
                account.setBalance(balance + (stockBid * stockBidVol));

                // set stock
                // no more stock in the market 
                stock.setAsk(0);
                stock.setAsk_volume(0);

            }
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/trades")
    public Trade createTrade(@Valid @RequestBody Trade trade){
        // check if logged in user == customerId in trade
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
        long loggedInCustomerId = customer.getId();

        if (loggedInCustomerId != trade.getCustomerId()) {
            throw new CustomerMismatchException();
        }

        // check if account belongs to customer
        long accountId = trade.getAccountId();

        List<Account> loggedInAccountList = accounts.findByCustomerId(loggedInCustomerId);

        if (loggedInAccountList.isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }

        boolean accountMatched = false;
        Account account = null;
        for (Account loggedInAcc : loggedInAccountList) {
            if (loggedInAcc.getId() == accountId) {
                account = loggedInAcc;
                accountMatched = true;
            }
        }

        if (!accountMatched) {
            throw new AccountMismatchException();
        }

        // check if quantity % 100 == 0 (if not bad request)
        int quantity = trade.getQuantity();
        if (quantity % 100 != 0) {
            throw new InvalidQuantityException();
        }

        // check if symbol exist in database
        String symbol = trade.getSymbol();
        if (stocks.findBySymbol(symbol) == null) {
            throw new InvalidStockException(symbol);
        }

        // check if action has the correct input
        String action = trade.getAction();
        if (!(action.equals("buy") || action.equals("sell"))) {
            throw new InvalidTradeException();
        }

        double bid = trade.getBid();
        double ask = trade.getAsk();
        double calculatedBuyPrice;

        CustomStock stock = stocks.findBySymbol(symbol)
            .orElseThrow(() -> new InvalidStockException(symbol));

            // if customer does not have a portfolio
        Portfolio portfolio = portfolios.findByCustomerId(customer.getId()) 
        .orElseThrow(() -> new PortfolioNotFoundException(customer.getId()));

        // check current time
        ZonedDateTime current = ZonedDateTime.now();
        int currentHour = current.getHour();
        int nineAM = 9;
        int fivePM = 17;
        String currentDay = current.getDayOfWeek().name();

        //if time is not within 9am and 5pm on weekdays, then trade not matched
        // if (currentHour >= fivePM || currentHour <= nineAM || currentDay.equals("saturday") || currentDay.equals("sunday")) {
        //     trade.setAccount(account);
        //     trade.setStock(stock);
        //     trade.setPortfolio(portfolio);
        //     trade.setStatus("open");
        //     updateTradeToStock(trade, stock);
        //     System.out.println("AFTER 5PM ALREADY");
        //     return trades.save(trade);
        // }

        if (action.equals("buy")) {
            // FOR BUYING
    
            // if it's a buy market order, change the bid value to current stock's ask price
            if (bid == 0.0) {
                bid = stock.getAsk();
            }

            calculatedBuyPrice = bid * quantity;
            
            // check if account has sufficient balance
            double available_balance = account.getAvailable_balance();

            if (calculatedBuyPrice > available_balance) {
                throw new InsufficientBalanceException();
            }
        
            List<Trade> specificStockOpenAndPartialFilledSellTrade = getSpecificStockOpenAndPartialFilledSellTrade(symbol);
            // check if the market has the stocks the customer is buying
            if (!(specificStockOpenAndPartialFilledSellTrade.isEmpty())) {
                // check price in previous trades (better price match first)
                buyTradeCheckForSellMatch(stock, trade, account, customer, calculatedBuyPrice);
            } else {
                // if stocks not matched and the customer's trade is the best price 
                // then update the stock with the customer's trade  
                trade.setStatus("open");
                updateTradeToStock(trade, stock);
                // if there's sufficient balance, set available balance to new balance
                account.setAvailable_balance(available_balance - calculatedBuyPrice);
            }
        } else {
            // FOR SELLING
            double stockBid = stock.getBid();
            long stockBidVol = stock.getBid_volume();

            // if it's a sell market order, change the ask value to current stock's bid price
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
        // limit order -> customer specify price (sell -> ask price, buy -> bid price) (expire at 5)
        // market order -> market price (ask and bid -> 0.0) (done immediately)

        // better price match first
        // if same price, then match earliest trade
        
        // FOR BUYING
        // DONE check if symbol exist in stock
        // DONE check if enough amount in account balance when submitting the order (must be sufficient, if not error 400)
        // check if amount is enough in balance when filling the order bc bid/ ask price will change (if not enough, partial filled)
        // add to portfolio

            // buy at market price -> market ask price
            // buy at limit bid price

        // FOR SELLING
        // check if symbol exist in portfolio (quantity selling > quantity own)

            // sell at market price -> market bid price
            // sell at limit ask price

        // set current timestamp to date
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        trade.setDate(timestamp.getTime());
        
        trade.setAccount(account);
        trade.setStock(stock);
        
        accounts.save(account);
        return trades.save(trade);
    }

    @PutMapping("/api/trades/{tradeId}")
    public Trade updateSpecificTrade(@PathVariable (value = "tradeId") Long tradeId, @Valid @RequestBody Trade updatedTradeInfo){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        // user that was authenticated
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
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