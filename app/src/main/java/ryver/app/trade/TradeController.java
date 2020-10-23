package ryver.app.trade;

import java.math.BigDecimal;
import java.math.MathContext;
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
import org.springframework.security.access.prepost.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@RestController
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

    // CREATED FOR TESTING. NOT NEEDED FOR SUBMISSION
    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/trades")
    public List<Trade> getAllTrades(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
        long customerId = customer.getId();

        return trades.findByCustomerId(customerId);

    }

    

    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/trades/{tradeId}")
    public Trade getSpecificTrade(@PathVariable (value = "tradeId") Long tradeId){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
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
            if ((trade.getBid() < stock.getAsk().doubleValue()) && (trade.getBid() > stock.getBid().doubleValue())) {
                System.out.println("ABCD1");
                stock.setBid(BigDecimal.valueOf(trade.getBid()));
                stock.setBid_volume(trade.getQuantity());
                stocks.save(stock);
            }
            
        } else {
            // if this trade's ask is higher than the stock's previous bid
            // if this trade's ask is lower than the stock's previous ask 
            // -> save new ask price and quantity into the stocks database
            if ((trade.getAsk() > stock.getBid().doubleValue()) && (trade.getAsk() < stock.getAsk().doubleValue())) {
                System.out.println("ABCD2");
                stock.setAsk(BigDecimal.valueOf(trade.getAsk()));
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

    public void buyTradeCheckForSellMatch(CustomStock stock, Trade trade, Account account, Customer customer, double calculatedBuyPrice) {
        double stockAsk = stock.getAsk().doubleValue();
        long stockAskVol = stock.getAsk_volume();

        double bid = trade.getBid();

        if (bid == 0.0) {
            bid = stock.getAsk().doubleValue();
        }
        int quantity = trade.getQuantity();

        double available_balance = account.getAvailable_balance();
        double balance = account.getBalance();

        // if trade's bid is higher than stock's ask -> match & buy
        if (bid >= stockAsk) {
            System.out.println("S");
            // if trade was not previously filled
            if (trade.getAvg_price() == 0.0) {
                System.out.println("T");
                trade.setAvg_price(stockAsk);
            } else {
                System.out.println("U");
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
                stock.setAsk(null);
                stock.setAsk_volume(0);

                // no more selling stock in the market 
                // throw new EmptySellingStockInMarket(); 
                return;
            }

            Comparator<Trade> compareByAsk = Comparator.comparing( Trade::getAsk );
            Comparator<Trade> compareByDate = Comparator.comparing( Trade::getDate );
            Comparator<Trade> compareByAskAndDate = compareByAsk.thenComparing(compareByDate);
            
            Collections.sort(tradeSellListOfSymbol, compareByAskAndDate);
            

            System.out.println("V");

            int tradeQuantity = quantity;
            checkTradeQuantityAgainstStockAskVol(stock, trade, account, customer, tradeSellListOfSymbol, tradeQuantity);
        } else {
            // if trade's bid is lower than stock's ask -> not matched
            System.out.println("AA");
            trade.setStatus("open");
            updateTradeToStock(trade, stock);
            // if there's sufficient balance, set available balance to new balance
            account.setAvailable_balance(available_balance - calculatedBuyPrice);
        }
    }

    public void checkTradeQuantityAgainstStockAskVol(CustomStock stock, Trade trade, Account account, Customer customer, List<Trade> tradeSellListOfSymbol, long tradeQuantity) {
        // final double prevStockAsk = stock.getAsk().doubleValue();
        double stockAsk = stock.getAsk().doubleValue();
        int stockAskVol = (int)stock.getAsk_volume();

        // double bid = trade.getBid();
        // int quantity = trade.getQuantity();

        int filledQuantity = trade.getFilled_quantity();

        double available_balance = account.getAvailable_balance();
        double balance = account.getBalance();

        // if stock's volume is enough to fill trade's quantity
        if (tradeQuantity <= (stockAskVol - filledQuantity)) {
            System.out.println("WWWW");
            trade.setStatus("filled");
            trade.setFilled_quantity(trade.getQuantity());
            trade.setAvg_price(stockAsk);
            stock.setLast_price(new BigDecimal(stockAsk, MathContext.DECIMAL64));

            account.setAvailable_balance(available_balance - (stockAsk * tradeQuantity));
            account.setBalance(balance - (stockAsk * tradeQuantity));

            Portfolio portfolio = customer.getPortfolio();
            createAsset(stock, trade, portfolio);
            createAsset(stock, tradeSellListOfSymbol.get(0), portfolio);

            // if the stock fills the trade just nice, revert the stock back to the previous stock
            if (tradeQuantity == stockAskVol) {
                System.out.println("X");
                tradeSellListOfSymbol.get(0).setStatus("filled");
                trade.setFilled_quantity(tradeSellListOfSymbol.get(0).getQuantity());

                
                // remove that trade from the list if its filled
                tradeSellListOfSymbol.remove(0);
                
                BigDecimal newStockAsk = new BigDecimal(tradeSellListOfSymbol.get(0).getAsk(), MathContext.DECIMAL64);
                long newStockAskVol = tradeSellListOfSymbol.get(0).getQuantity();
                stock.setAsk(newStockAsk);
                stock.setAsk_volume(newStockAskVol);
            
            } else {
                System.out.println("Y");
                // if there's still quantity leftover in stock
                stock.setAsk_volume(stockAskVol - tradeQuantity);

                tradeSellListOfSymbol.get(0).setStatus("partial-filled");
                tradeSellListOfSymbol.get(0).setFilled_quantity((int)tradeQuantity);
                tradeSellListOfSymbol.get(0).setAvg_price(stockAsk);
                
            }
        } else {
            // if the quantity in the stocks is not enough to fill the trade

            // set the current best trade (stock) to filled
            System.out.println("Z");
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
            createAsset(stock, tradeSellListOfSymbol.get(0), portfolio);

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
                    BigDecimal newStockAsk = new BigDecimal(tradeSellListOfSymbol.get(j).getAsk(), MathContext.DECIMAL64);
                    long newStockAskVol = (int)tradeSellListOfSymbol.get(j).getQuantity();
                    stock.setAsk(newStockAsk);
                    stock.setAsk_volume(newStockAskVol);

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
                            account.setAvailable_balance(available_balance - (stockAsk * stockAskVol) - (stock.getAsk().doubleValue() * (tradeQuantity - stockAskVol)));
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
                            account.setAvailable_balance(available_balance - (stockAsk * stockAskVol) - (stock.getAsk().doubleValue() * (tradeQuantity - stockAskVol)));
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
                account.setAvailable_balance(available_balance - (stockAsk * stockAskVol) - (stock.getAsk().doubleValue() * (tradeQuantity - stockAskVol)));
                account.setBalance(balance - (stockAsk * stockAskVol));

                // set stock
                stock.setAsk(null);
                stock.setAsk_volume(0);

                // no more selling stock in the market 
                // throw new EmptySellingStockInMarket(); 
            }

            

        }
    }

    private void createAsset(CustomStock stock, Trade trade, Portfolio portfolio) {
        // for buying
        System.out.println("LALAA");
        if (trade.getAction().equals("buy")) {
            double bid = trade.getBid();
            if (bid == 0.0) {
                bid = stock.getAsk().doubleValue();
            }
            System.out.println("1LALAA");

            long portfolioId = portfolio.getId();
            String code = trade.getSymbol();   
            System.out.println("Here");
            
            System.out.println(assets.findByCodeAndPortfolioId(code, portfolioId));
            System.out.println("Here2");

            Optional<Asset> nothing = Optional.empty();
            System.out.println(nothing);
            // asset already exist in portfolio -> update asset
            if (assets.findByCodeAndPortfolioId(code, portfolioId) != nothing) {
                System.out.println("2LALAA");
                Asset asset = assets.findByCodeAndPortfolioId(code, portfolioId)
                    .orElseThrow(() -> new AssetCodeNotFoundException(code));

                long assetId = asset.getId();
                int prevQuantity = asset.getQuantity();
                double prevAvg_price = asset.getAvg_price().doubleValue();
                double prevTotalPrice = prevQuantity * prevAvg_price;
                System.out.println("3LALAA");
                
                int newQuantity = prevQuantity + trade.getFilled_quantity();
                double newTotalPrice = prevTotalPrice + (trade.getFilled_quantity() * bid);
                double newAvg_price = newTotalPrice / newQuantity;
                System.out.println(prevQuantity);
                System.out.println(prevQuantity);
                

                Asset newAsset = asset;
                newAsset.setQuantity(newQuantity);
                newAsset.setAvg_price(new BigDecimal(newAvg_price, MathContext.DECIMAL64));
                assetCtrl.updateAsset(portfolioId, assetId, newAsset);
                System.out.println("5LALAA");

            } else {
                // asset does not exist in portfolio -> add trade to asset
                System.out.println("6LALAA");
                int quantity = trade.getFilled_quantity();
                BigDecimal avg_price = stock.getAsk();
                BigDecimal current_price = stock.getAsk();
                double value = current_price.doubleValue() * quantity;
                BigDecimal gain_loss = new BigDecimal(value - (avg_price.doubleValue() * quantity), MathContext.DECIMAL64);
                System.out.println("7LALAA");
                
    
                Asset asset = new Asset(code, quantity, avg_price, current_price, value, gain_loss);
                asset.setPortfolio(portfolio);
                assetCtrl.addAsset(portfolioId, asset);
                System.out.println("8LALAA");
            }
        }

        // // update total and unrealized gain loss in portfolio
        // Portfolio updatedPortfolio = portfolio;
        // updatedPortfolio.setTotal_gain_loss(total_gain_loss);
        // portfolioCtrl.updatePortfolio(portfolioId, updatedPortfolio);
        
    }

    private void deleteAsset(CustomStock stock, Trade trade, Portfolio portfolio) {
        // for selling
        if (trade.getAction().equals("sell")) {
            double ask = trade.getAsk();
            if (ask == 0.0) {
                ask = stock.getBid().doubleValue();
            }

            long portfolioId = portfolio.getId();
            String code = trade.getSymbol();

            Asset asset = assets.findByCodeAndPortfolioId(code, portfolioId)
                .orElseThrow(() -> new AssetCodeNotFoundException(code));

            long assetId = asset.getId();

            // if trade quantity < asset quantity -> minus
            if (trade.getQuantity() < asset.getQuantity()) {
                int prevQuantity = asset.getQuantity();
                int newQuantity = prevQuantity - trade.getFilled_quantity();

                Asset newAsset = asset;
                newAsset.setQuantity(newQuantity);
                
                assetCtrl.updateAsset(portfolioId, assetId, newAsset);
                
            } else {
                // if trade quantity == asset quantity -> delete
                assetCtrl.deleteAsset(portfolioId, assetId);
            }

            // update total gain loss in portfolio
            double prevTotalGainLoss = portfolio.getTotal_gain_loss();
            double newTotalGainLoss = prevTotalGainLoss + asset.getGain_loss().doubleValue();

            Portfolio updatedPortfolio = portfolio;
            updatedPortfolio.setTotal_gain_loss(newTotalGainLoss);
            portfolioCtrl.updatePortfolio(portfolioId, updatedPortfolio);

        }
        
    }

    

    public void sellTradeCheckForBuyMatch(CustomStock stock, Trade trade, Account account, Customer customer) {
        double stockBid = stock.getBid().doubleValue();
        long stockBidVol = stock.getBid_volume();

        double ask = trade.getAsk();

        if (ask == 0.0) {
            ask = stock.getBid().doubleValue();
        }
        int quantity = trade.getQuantity();

        double available_balance = account.getAvailable_balance();
        double balance = account.getBalance();

        // if trade's ask is lower than stock's bid -> match & sell
        if (ask <= stockBid) {
            System.out.println("S");
            // if trade was not previously filled
            if (trade.getAvg_price() == 0.0) {
                System.out.println("T");
                trade.setAvg_price(stockBid);
            } else {
                System.out.println("U");
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
                stock.setBid(null);
                stock.setBid_volume(0);

                // no more selling stock in the market 
                // throw new EmptySellingStockInMarket(); 
                return;
            }

            Comparator<Trade> compareByBid = Comparator.comparing( Trade::getBid );
            Comparator<Trade> compareByDate = Comparator.comparing( Trade::getDate );
            Comparator<Trade> compareByBidAndDate = compareByBid.thenComparing(compareByDate);
            
            Collections.sort(tradeBuyListOfSymbol, compareByBidAndDate);
            

            System.out.println("V");

            int tradeQuantity = quantity;
            checkTradeQuantityAgainstStockBidVol(stock, trade, account, customer, tradeBuyListOfSymbol, tradeQuantity);
        } else {
            // if trade's ask is higher than stock's bid -> not matched
            System.out.println("AA");
            trade.setStatus("open");
            updateTradeToStock(trade, stock);
        }
    }

    public void checkTradeQuantityAgainstStockBidVol(CustomStock stock, Trade trade, Account account, Customer customer, List<Trade> tradeBuyListOfSymbol, long tradeQuantity) {
        double stockBid = stock.getBid().doubleValue();
        int stockBidVol = (int)stock.getBid_volume();

        // double bid = trade.getBid();
        // int quantity = trade.getQuantity();

        int filledQuantity = trade.getFilled_quantity();

        double available_balance = account.getAvailable_balance();
        double balance = account.getBalance();

        // if stock's volume is enough to fill trade's quantity
        if (tradeQuantity <= (stockBidVol - filledQuantity)) {
            System.out.println("W");
            trade.setStatus("filled");
            trade.setFilled_quantity(trade.getQuantity());
            trade.setAvg_price(stockBid);

            account.setAvailable_balance(available_balance + (stockBid * tradeQuantity));
            account.setBalance(balance + (stockBid * tradeQuantity));
            
            // delete entry from portfolio
            Portfolio portfolio = customer.getPortfolio();
            deleteAsset(stock, trade, portfolio);
            deleteAsset(stock, tradeBuyListOfSymbol.get(0), portfolio);

            // if the stock fills the trade just nice, revert the stock back to the previous stock
            if (tradeQuantity == stockBidVol) {
                System.out.println("X");
                tradeBuyListOfSymbol.get(0).setStatus("filled");
                trade.setFilled_quantity(tradeBuyListOfSymbol.get(0).getQuantity());
                
                
                // remove that trade from the list if its filled
                tradeBuyListOfSymbol.remove(0);
                
                BigDecimal newStockBid = new BigDecimal(tradeBuyListOfSymbol.get(0).getBid(), MathContext.DECIMAL64);
                long newStockBidVol = tradeBuyListOfSymbol.get(0).getQuantity();
                stock.setBid(newStockBid);
                stock.setBid_volume(newStockBidVol);
            
            } else {
                System.out.println("Y");
                // if there's still quantity leftover in stock
                stock.setBid_volume(stockBidVol - tradeQuantity);
                // minus from assets

                tradeBuyListOfSymbol.get(0).setStatus("partial-filled");
                tradeBuyListOfSymbol.get(0).setFilled_quantity((int)tradeQuantity);
                tradeBuyListOfSymbol.get(0).setAvg_price(stockBid);
                
            }
        } else {
            // if the quantity in the stocks is not enough to fill the trade

            // set the current best trade (stock) to filled
            System.out.println("Z");
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
            deleteAsset(stock, tradeBuyListOfSymbol.get(0), portfolio);
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
                    BigDecimal newStockBid = new BigDecimal(tradeBuyListOfSymbol.get(j).getBid(), MathContext.DECIMAL64);
                    long newStockBidVol = (int)tradeBuyListOfSymbol.get(j).getQuantity();
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
                // available balance - stock bought - leftover open stocks
                account.setAvailable_balance(available_balance - (stockBid * stockBidVol) - (stock.getAsk().doubleValue() * (tradeQuantity - stockBidVol)));
                account.setBalance(balance - (stockBid * stockBidVol));

                // set stock
                stock.setAsk(null);
                stock.setAsk_volume(0);

                // no more selling stock in the market 
                // throw new EmptySellingStockInMarket(); 
            }

            

        }
    }

    @PreAuthorize("authentication.principal.active == true")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/trades")
    public Trade createTrade(@Valid @RequestBody Trade trade){
        
        System.out.println("A");

        // check if logged in user == customerId
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
        long loggedInCustomerId = customer.getId();

        System.out.println("B");
        if (loggedInCustomerId != trade.getCustomerId()) {
            throw new CustomerMismatchException();
        }

        System.out.println("C");
        // check if account belongs to customer
        long accountId = trade.getAccountId();

        List<Account> loggedInAccountList = accounts.findByCustomerId(loggedInCustomerId);

        System.out.println("D");
        if (loggedInAccountList.isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }

        System.out.println("E");
        boolean accountMatched = false;
        Account account = null;
        for (Account loggedInAcc : loggedInAccountList) {
            if (loggedInAcc.getId() == accountId) {
                account = loggedInAcc;
                accountMatched = true;
            }
        }

        System.out.println("F");
        if (!accountMatched) {
            throw new AccountMismatchException();
        }

        System.out.println("G");
        // check if quantity % 100 == 0 (if not bad request)
        int quantity = trade.getQuantity();
        if (quantity % 100 != 0) {
            throw new InvalidQuantityException();
        }

        System.out.println("H");
        // check if symbol exist in database
        String symbol = trade.getSymbol();
        if (stocks.findBySymbol(symbol) == null) {
            throw new InvalidStockException(symbol);
        }

        System.out.println("I");
        // check if action has the correct input
        String action = trade.getAction();
        if (!(action.equals("buy") || action.equals("sell"))) {
            throw new InvalidTradeException();
        }

        double bid = trade.getBid();
        double ask = trade.getAsk();
        double calculatedBuyPrice;

        System.out.println("J");
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

        // if time is not within 9am and 5pm on weekdays, then trade not matched
        // if (currentHour >= fivePM || currentHour <= nineAM || currentDay.equals("saturday") || currentDay.equals("sunday")) {
        //     trade.setAccount(account);
        //     trade.setStock(stock);
        //     trade.setPortfolio(portfolio);
        //     trade.setStatus("open");
        //     System.out.println("AFTER 5PM ALREADY");
        //     return trades.save(trade);
        // }

        System.out.println("K");
        if (action.equals("buy")) {
            // FOR BUYING
            System.out.println("L");

            // if it's a buy market order, change the bid value to current stock's ask price
            if (bid == 0.0) {
                bid = stock.getAsk().doubleValue();
            }

            calculatedBuyPrice = bid * quantity;
            
            System.out.println("O");
            // check if account has sufficient balance
            double available_balance = account.getAvailable_balance();

            if (calculatedBuyPrice > available_balance) {
                throw new InsufficientBalanceException();
            }

            System.out.println("P");
            
            
            System.out.println("Q");
            List<Trade> specificStockOpenAndPartialFilledSellTrade = getSpecificStockOpenAndPartialFilledSellTrade(symbol);

            // check if the market has the stocks the customer is buying
            if (!(specificStockOpenAndPartialFilledSellTrade.isEmpty())) {
                System.out.println("R");
                // check price in previous trades (better price match first)
                buyTradeCheckForSellMatch(stock, trade, account, customer, calculatedBuyPrice);


            } else {
                // if stocks not matched and the customer's trade is the best price 
                // then update the stock with the customer's trade  
                System.out.println("BB");
                trade.setStatus("open");
                updateTradeToStock(trade, stock);
                // if there's sufficient balance, set available balance to new balance
                account.setAvailable_balance(available_balance - calculatedBuyPrice);
            }
        } else {
            // FOR SELLING
            System.out.println("0000");
            double stockBid = stock.getBid().doubleValue();
            long stockBidVol = stock.getBid_volume();

            // if it's a sell market order, change the ask value to current stock's bid price
            if (ask == 0.0) {
                ask = stockBid;
            }
            System.out.println("0001");

            long portfolioId = portfolio.getId();

            // if customer does not have the same stock in portofolio
            Asset asset = assets.findByCodeAndPortfolioId(symbol, portfolioId)
                .orElseThrow(() -> new InvalidStockException(symbol));

            System.out.println("0002");
            System.out.println(asset.getQuantity());
            System.out.println(trade.getQuantity());
            
            // if quantity in portfolio is lesser than trade quantity, then throw exception
            if (trade.getQuantity() > asset.getQuantity()) {
                throw new InsufficientStockException();
            }

            List<Trade> specificStockOpenAndPartialFilledBuyTrade = getSpecificStockOpenAndPartialFilledBuyTrade(symbol);
            System.out.println("0003");

            // check if the market has the buying stocks the customer is selling
            if (!(specificStockOpenAndPartialFilledBuyTrade.isEmpty())) {
                System.out.println("R");
                // check price in previous trades (better price match first)
                sellTradeCheckForBuyMatch(stock, trade, account, customer);

            } else {
                // if stocks not matched and the customer's trade is the best price 
                // then update the stock with the customer's trade  
                System.out.println("BB");
                trade.setStatus("open");
                updateTradeToStock(trade, stock);
            }

        }
        
        

        System.out.println("CC");

        

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

    @PreAuthorize("authentication.principal.active == true")
    @PutMapping("/trades/{tradeId}")
    public Trade updateSpecificTrade(@PathVariable (value = "tradeId") Long tradeId, @Valid @RequestBody Trade updatedTradeInfo){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
        long customerId = customer.getId();

        Trade trade = trades.findByIdAndCustomerId(tradeId, customerId)
            .orElseThrow(() -> new TradeNotFoundException(tradeId));


        // customer can cancel a trade if its open
        if (trade.getStatus().equals("open") && updatedTradeInfo.getStatus().equals("cancelled")) {
            trade.setStatus("cancelled");
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