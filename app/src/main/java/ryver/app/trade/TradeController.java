package ryver.app.trade;

import ryver.app.customer.*;
import ryver.app.account.*;
import ryver.app.transaction.*;
import ryver.app.stock.*;
import ryver.app.asset.Asset;
import ryver.app.asset.AssetCodeNotFoundException;
import ryver.app.asset.AssetController;
import ryver.app.asset.AssetRepository;
import ryver.app.portfolio.*;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    public TradeController(TradeRepository trades, CustomerRepository customers, AccountRepository accounts,
            StockRepository stocks, PortfolioRepository portfolios, PortfolioController portfolioCtrl,
            AssetRepository assets, AssetController assetCtrl) {
        this.trades = trades;
        this.customers = customers;
        this.accounts = accounts;
        this.stocks = stocks;
        this.portfolios = portfolios;
        this.assets = assets;
        this.portfolioCtrl = portfolioCtrl;
        this.assetCtrl = assetCtrl;
    }

    /***************************************************************************
     * CHECKING METHODS
     ***************************************************************************/

    /**
     * Get the Customer who is currently logged in
     * 
     * @return Customer
     */
    public Customer getLoggedinUser() {
        String authorisedUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customers.findByUsername(authorisedUserName)
                .orElseThrow(() -> new CustomerNotFoundException(authorisedUserName));

        checkCustomerActive(customer);
        return customer;
    }

    /**
     * Check if the customer is active If deactivated - return 404
     * 
     * @param customer
     */
    private void checkCustomerActive(Customer customer) {
        if (!customer.isActive()) {
            throw new InactiveCustomerException();
        }
    }

    /**
     * Check if the accountId specified by the Customer belongs to the Customer
     * 
     * @return Account
     */
    public Account getAccountIfMatch(Trade trade) {
        Customer loggedInCustomer = getLoggedinUser();
        long loggedInCustomerId = loggedInCustomer.getId();

        // check if account belongs to customer
        long accountId = trade.getAccountId();
        // 1. check if account exists
        accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        // 2. if account exists, check if customer owns the account
        Account account = accounts.findByIdAndCustomerId(accountId, loggedInCustomerId)
                .orElseThrow(() -> new AccountMismatchException());

        return account;
    }

    /***************************************************************************
     * GET TRADE
     ***************************************************************************/

    /**
     * Get all Trades associated with the logged in Customer's ID Returns 200 OK (if
     * no exceptions)
     * 
     * @return List<Trade>
     */
    @GetMapping("/api/trades")
    public List<Trade> getAllTrades() {
        Customer customer = getLoggedinUser();
        long customerId = customer.getId();
        return trades.findByCustomerId(customerId);
    }

    /**
     * Get specific Trade associated with the logged in Customer's ID that has the
     * specified TradeId Returns 200 OK (if no exceptions)
     * 
     * @param tradeId
     * @return Trade
     */
    @GetMapping("/api/trades/{tradeId}")
    public Trade getSpecificTrade(@PathVariable(value = "tradeId") Long tradeId) {
        Customer customer = getLoggedinUser();
        long customerId = customer.getId();
        Trade trade = trades.findByIdAndCustomerId(tradeId, customerId)
                .orElseThrow(() -> new TradeNotFoundException(tradeId));
        return trade;
    }

    /***************************************************************************
     * CANCEL TRADE
     ***************************************************************************/

    /**
     * Cancel a specific Trade, based on JSON data Filled or partial filled Trades
     * cannot be cancelled Returns 200 OK (if no exceptions)
     * 
     * @param tradeId
     * @param updatedTradeInfo
     * @return Trade
     */
    @PutMapping("/api/trades/{tradeId}")
    public Trade cancelTrade(@PathVariable(value = "tradeId") Long tradeId,
            @Valid @RequestBody Trade updatedTradeInfo) {

        Customer customer = getLoggedinUser();
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

        Account account = getAccountIfMatch(updatedTradeInfo);

        // set back the available balance if its a buy order
        if (trade.getAction().equals("buy")) {
            account.setAvailable_balance(account.getBalance());
        }

        trades.save(trade);
        accounts.save(account);
        return trade;
    }

    /***************************************************************************
     * ASSET
     ***************************************************************************/

    /**
     * Create an Asset if the buy order gets filled
     * 
     * @param stock
     * @param trade
     * @param portfolio
     */
    private void createAsset(CustomStock stock, Trade trade, Portfolio portfolio, int amountTraded, String from) {
        // for buying
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

            // after buying more
            long assetId = asset.getId();
            Asset newAsset;
            if (from.equals("fromBuy")) {
                newAsset = updatedAsset(stock, trade, stock.getAsk(), code, asset, amountTraded);
            } else {
                newAsset = updatedAsset(stock, trade, trade.getBid(), code, asset, amountTraded);
            }

            assetCtrl.updateAsset(portfolioId, assetId, newAsset);
        } else {
            // asset does not exist in portfolio -> add trade to asset
            int quantity = trade.getFilled_quantity();
            double avg_price;

            if (from.equals("fromBuy")) {
                avg_price = stock.getAsk();
            } else {
                avg_price = trade.getBid();
            }
            double current_price = stock.getLast_price();
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

    /**
     * Update existing assets with the new information
     * 
     * @param stock
     * @param trade
     * @param price
     * @param code
     * @param asset
     * @param amountTraded
     * @return Asset
     */
    private Asset updatedAsset(CustomStock stock, Trade trade, double price, String code, Asset asset,
            int amountTraded) {
        int prevQuantity = asset.getQuantity();
        double prevAvg_price = asset.getAvg_price();
        double prevTotalPrice = prevQuantity * prevAvg_price;
        double newTotalPrice;
        int newQuantity;
        if (trade.getAction().equals("buy")) {
            newQuantity = prevQuantity + amountTraded;
            newTotalPrice = prevTotalPrice + (amountTraded * price);

        } else {
            newQuantity = prevQuantity - amountTraded;
            newTotalPrice = prevTotalPrice - (amountTraded * price);
        }

        double newAvg_price = Math.round((newTotalPrice / newQuantity) * 100.0) / 100.0;
        // double newAvg_price = (newTotalPrice / newQuantity);

        double currentPrice = stock.getLast_price();

        double value = currentPrice * newQuantity;
        double gain_loss = value - (newAvg_price * newQuantity);

        Asset newAsset = new Asset(code, newQuantity, newAvg_price, currentPrice, value, gain_loss);
        return newAsset;
    }

    /**
     * Delete the Asset if the sell order gets filled and quantity becomes 0
     * 
     * @param stock
     * @param trade
     * @param portfolio
     */
    private void deleteAsset(CustomStock stock, Trade trade, Portfolio portfolio, int amountTraded, String from) {
        // for selling
        double ask = trade.getAsk();
        if (ask == 0.0) {
            ask = stock.getBid();
        }

        long portfolioId = portfolio.getId();
        String code = trade.getSymbol();

        Asset asset = assets.findByCodeAndPortfolioId(code, portfolioId)
                .orElseThrow(() -> new AssetCodeNotFoundException(code));

        double realizedGainLoss;
        if (from.equals("fromSell")) {
            realizedGainLoss = (amountTraded * stock.getBid()) - (asset.getAvg_price() * amountTraded);
        } else { // "fromBuy"
            realizedGainLoss = (amountTraded * ask) - (asset.getAvg_price() * amountTraded);
        }

        long assetId = asset.getId();

        // if trade quantity < asset quantity -> minus
        if (trade.getFilled_quantity() < asset.getQuantity()) {

            Asset updatedAsset = updatedAsset(stock, trade, stock.getBid(), code, asset, amountTraded);

            assetCtrl.updateAsset(portfolioId, assetId, updatedAsset);

        } else {
            // if trade quantity == asset quantity -> delete
            assetCtrl.deleteAsset(portfolioId, assetId);
        }

        Portfolio updatedPortfolio = portfolio;
        updatedPortfolio.setRealized_gain_loss(portfolio.getRealized_gain_loss() + realizedGainLoss);
        // updatedPortfolio.setTotal_gain_loss(newTotalGainLoss);
        portfolioCtrl.updatePortfolio(portfolioId, updatedPortfolio);
    }

    /***************************************************************************
     * CREATE TRADE
     ***************************************************************************/

    /**
     * Create a new Trade using the JSON data Returns 201 Created (if no exceptions)
     * 
     * @param trade
     * @return Trade
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/trades")
    public Trade createTrade(@Valid @RequestBody Trade trade) {
        // check if logged in user == customerId in trade
        Customer customer = getLoggedinUser();
        long loggedInCustomerId = customer.getId();

        if (loggedInCustomerId != trade.getCustomerId()) {
            throw new CustomerMismatchException();
        }

        // check if account belongs to customer
        Account account = getAccountIfMatch(trade);

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

        // check if symbol exist in database
        String symbol = trade.getSymbol();
        CustomStock stock = stocks.findBySymbol(symbol).orElseThrow(() -> new InvalidStockException(symbol));

        // check if customer have a portfolio
        Portfolio portfolio = portfolios.findByCustomerId(customer.getId())
                .orElseThrow(() -> new PortfolioNotFoundException(customer.getId()));

        // check current time
        ZonedDateTime current = ZonedDateTime.now();
        int currentHour = current.getHour();
        final int nineAM = 9;
        final int fivePM = 17;
        String currentDay = current.getDayOfWeek().name();

        // if day is saturday or sunday
        // if hour is before 9am and after 5pm
        // then trade not matched
        // if ((currentDay.equals("SATURDAY") || currentDay.equals("SUNDAY"))
        // || (currentHour < nineAM || currentHour >= fivePM)) {
        // trade.setAccount(account);
        // trade.setStock(stock);
        // trade.setPortfolio(portfolio);
        // trade.setStatus("open");
        // updateTradeToStock(trade, stock);
        // return trades.save(trade);
        // }

        if (action.equals("buy")) {
            buy(trade, customer, account, stock);
        } else {
            sell(trade, customer, account, stock);
        }

        // set current timestamp to date
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        trade.setDate(timestamp.getTime());

        trade.setAccount(account);
        trade.setStock(stock);

        accounts.save(account);
        return trades.save(trade);
    }

    /***************************************************************************
     * BUY ORDERS
     ***************************************************************************/

    /**
     * Get all open or partial filled sell Trades associated with the specified
     * symbol
     * 
     * @param symbol
     * @return List<Trade>
     */
    public List<Trade> getOpenAndPartialFilledSellTradeList(String symbol) {
        List<Trade> tradeOpen = trades.findByActionAndStatusAndSymbol("sell", "open", symbol);
        List<Trade> tradePartialFilled = trades.findByActionAndStatusAndSymbol("sell", "partial-filled", symbol);
        List<Trade> tradeList = Stream.concat(tradeOpen.stream(), tradePartialFilled.stream())
                .collect(Collectors.toList());

        sortTradeList(tradeList);
        return tradeList;
    }


    
    /** 
     * Fill the buy order and add the asset
     * 
     * @param trade
     * @param customer
     * @param account
     * @param stock
     * @param quantityLeftToFill
     */
    public void fillMyBuyTrade(Trade trade, Customer customer, Account account, CustomStock stock,
            int quantityLeftToFill) {
        double stockAsk = stock.getAsk();
        double available_balance = account.getAvailable_balance();
        double balance = account.getBalance();

        trade.setStatus("filled");
        trade.setFilled_quantity(trade.getQuantity());
        stock.setLast_price(stockAsk);

        account.setAvailable_balance(available_balance - (stockAsk * quantityLeftToFill));
        account.setBalance(balance - (stockAsk * quantityLeftToFill));

        Portfolio portfolio = customer.getPortfolio();
        createAsset(stock, trade, portfolio, quantityLeftToFill, "fromBuy");
    }

    
    /** 
     * Fill the sell order and remove the asset
     * 
     * @param sellTrade
     * @param stock
     * @param sellList
     */
    public void fillStockSellTrade(Trade sellTrade, CustomStock stock, List<Trade> sellList) {
        double stockAsk = stock.getAsk();
        int quantity = sellTrade.getQuantity();

        Account sellTradeAccount = sellTrade.getAccount();
        double available_balance = sellTradeAccount.getAvailable_balance();
        double balance = sellTradeAccount.getBalance();

        sellTradeAccount.setAvailable_balance(available_balance + (stockAsk * quantity));
        sellTradeAccount.setBalance(balance + (stockAsk * quantity));

        sellTrade.setStatus("filled");
        sellTrade.setFilled_quantity(quantity);
        stock.setLast_price(stockAsk);

        long sellTradeCustomerId = sellTrade.getCustomerId();
        Customer sellTradeCustomer = customers.findById(sellTradeCustomerId).orElse(null);
        Portfolio portfolio = sellTradeCustomer.getPortfolio();
        deleteAsset(stock, sellTrade, portfolio, quantity, "fromBuy");

        // remove the first trade because it's filled
        sellList.remove(0);

        if (sellList.isEmpty()) {
            stock.setAsk_volume(0);
        } else {
            stock.setAsk(sellList.get(0).getAsk());
            stock.setAsk_volume(sellList.get(0).getQuantity());
        }
    }

    /**
     * For buy orders Check if there is a sell trade that matches the current buy
     * trade
     * 
     * @param stock
     * @param trade
     * @param account
     * @param customer
     * @param calculatedBuyPrice
     */
    public void buyMatch(CustomStock stock, Trade trade, Account account, Customer customer, double tradeBid,
            int quantityLeftToFill, int afterFilledQuantity, double prevTotalPrice, List<Trade> sellList) {

        trade.setStatus("open");
        double stockAsk = stock.getAsk();
        int quantity = trade.getQuantity();
        int prevFilledQuantity = trade.getFilled_quantity();

        double available_balance = account.getAvailable_balance();

        // if trade was not previously filled
        if (trade.getAvg_price() == 0.0) {
            trade.setAvg_price(stockAsk);
        } else {
            // if trade was previously filled
            // calculate new average price
            double previousPrice = trade.getAvg_price() * prevFilledQuantity;
            double newAvgPrice = previousPrice + (stockAsk * quantity);
            trade.setAvg_price(newAvgPrice);
            trade.setFilled_quantity(prevFilledQuantity + quantity);
        }

        if (sellList.isEmpty()) {
            // no more stock in the market
            stock.setAsk_volume(0);
            return;
        }

        double balance = account.getBalance();
        int stockAskVol = stock.getAsk_volume();

        // check for quantity
        // if my trade == stock vol -> stock trade filled, my trade filled
        // else if my trade > stock vol -> stock trade filled
        // else if my trade < stock vol -> my trade filled
        // loop again

        Trade sellTrade = sellList.get(0);
        Portfolio tradeSellPortfolio = customers.findById(sellTrade.getCustomerId()).orElse(null).getPortfolio();
        double price = stockAsk * quantityLeftToFill;

        // if have enough balance to buy
        if (available_balance >= price) {
            if (quantityLeftToFill == (stockAskVol - prevFilledQuantity)) {
                // buy trade is filled
                fillMyBuyTrade(trade, customer, account, stock, quantityLeftToFill);

                // stock trade is filled and removed from list
                fillStockSellTrade(sellTrade, stock, sellList);

            } else if (quantityLeftToFill > (stockAskVol - prevFilledQuantity)) {

                // buy trade is partial filled
                trade.setFilled_quantity(stockAskVol);
                afterFilledQuantity = trade.getFilled_quantity();
                quantityLeftToFill = quantity - afterFilledQuantity;
                prevTotalPrice = (afterFilledQuantity - prevFilledQuantity) * stockAsk;
                account.setAvailable_balance(account.getAvailable_balance() - prevTotalPrice);
                account.setBalance(account.getBalance() - prevTotalPrice);

                Portfolio portfolio = customer.getPortfolio();
                createAsset(stock, trade, portfolio, stockAskVol, "fromBuy");
                // stock trade is filled and removed from list
                fillStockSellTrade(sellTrade, stock, sellList);

                buyMatch(stock, trade, account, customer, tradeBid, quantityLeftToFill, afterFilledQuantity,
                        prevTotalPrice, sellList);

            } else if (quantityLeftToFill < (stockAskVol - prevFilledQuantity)) {
                // buy trade is filled
                fillMyBuyTrade(trade, customer, account, stock, quantityLeftToFill);

                // stock trade is partial filled
                stock.setAsk_volume(stockAskVol - quantityLeftToFill);

                Account sellTradeAccount = sellTrade.getAccount();
                sellTradeAccount.setAvailable_balance(available_balance + price);
                sellTradeAccount.setBalance(balance + price);

                sellTrade.setStatus("partial-filled");
                sellTrade.setFilled_quantity(quantityLeftToFill);
                sellTrade.setAvg_price(stockAsk);

                deleteAsset(stock, sellTrade, tradeSellPortfolio, quantityLeftToFill, "fromBuy");
            }
        } else {
            // if dont have enough balance to buy
            trade.setStatus("partial-filled");
            double new_available_balance = account.getAvailable_balance();
            double new_balance = account.getBalance();

            int quantityCanBuy = (int) (new_available_balance / stockAsk);
            // convert to an integer in 100s
            quantityCanBuy = quantityCanBuy / 100 * 100;
            double currentPrice = quantityCanBuy * stockAsk;

            if (quantityCanBuy == 0) {
                return;
            } else {
                int totalFilledQuantity = afterFilledQuantity + quantityCanBuy;
                trade.setFilled_quantity(totalFilledQuantity);
                double avg_price = Math
                        .round((prevTotalPrice + quantityCanBuy * stockAsk) / (totalFilledQuantity) * 100.0) / 100.0;
                // double avg_price = (prevTotalPrice + quantityCanBuy * stockAsk) /
                // (totalFilledQuantity);

                trade.setAvg_price(avg_price);

                account.setAvailable_balance(new_available_balance - currentPrice);
                account.setBalance(new_balance - currentPrice);

                Portfolio portfolio = customer.getPortfolio();
                createAsset(stock, trade, portfolio, quantityCanBuy, "fromBuy");
            }

            sellTrade = sellList.get(0);
            sellTrade.setFilled_quantity(sellTrade.getFilled_quantity() + quantityCanBuy);
            Account sellTradeAccount = sellTrade.getAccount();

            sellTradeAccount.setAvailable_balance(sellTradeAccount.getAvailable_balance() + currentPrice);
            sellTradeAccount.setBalance(sellTradeAccount.getBalance() + currentPrice);

            Customer sellTradeCustomer = sellTradeAccount.getCustomer();
            Portfolio sellTradePortfolio = sellTradeCustomer.getPortfolio();

            deleteAsset(stock, sellTrade, sellTradePortfolio, quantityCanBuy, "fromBuy");
        }
    }

    /**
     * Create a new buy trade
     * 
     * @param trade
     * @param customer
     * @param account
     * @param stock
     */
    public void buy(Trade trade, Customer customer, Account account, CustomStock stock) {
        double tradeBid = trade.getBid();
        int quantity = trade.getQuantity();
        double stockAsk = stock.getAsk();

        // if order is market buy OR
        // if order is limit buy but tradeBid is higher than market's ask
        if (tradeBid == 0 || tradeBid > stockAsk)
            tradeBid = stockAsk;

        double calculatedBuyPrice = tradeBid * quantity;
        // check if account has sufficient balance
        double available_balance = account.getAvailable_balance();
        if (calculatedBuyPrice > available_balance) {
            throw new InsufficientBalanceException();
        }

        // get sell list
        List<Trade> sortedOpenAndPartialFilledSellTradeList = getOpenAndPartialFilledSellTradeList(trade.getSymbol());

        // if sell list is not empty
        if (!(sortedOpenAndPartialFilledSellTradeList.isEmpty())) {
            if (tradeBid >= stockAsk) {
                int quantityLeftToFill = quantity;
                buyMatch(stock, trade, account, customer, tradeBid, quantityLeftToFill, 0, 0,
                        sortedOpenAndPartialFilledSellTradeList);
            } else {
                // if stocks not matched and the customer's trade is the best price
                // then update the stock with the customer's trade
                trade.setStatus("open");
                updateTradeToStock(trade, stock);
            }
        } else {
            // if sell list is empty -> trade not matched -> return

            // update market best price (stock)
            updateTradeToStock(trade, stock);
            trade.setStatus("open");
            // set new available balance
            account.setAvailable_balance(available_balance - calculatedBuyPrice);
            return;
        }

    }

    /***************************************************************************
     * SELL ORDERS
     ***************************************************************************/

    /**
     * Get all open or partial filled buy Trades associated with the specified
     * symbol
     * 
     * @param symbol
     * @return List<Trade>
     */
    public List<Trade> getOpenAndPartialFilledBuyTradeList(String symbol) {
        List<Trade> tradeOpen = trades.findByActionAndStatusAndSymbol("buy", "open", symbol);
        List<Trade> tradePartialFilled = trades.findByActionAndStatusAndSymbol("buy", "partial-filled", symbol);
        List<Trade> tradeList = Stream.concat(tradeOpen.stream(), tradePartialFilled.stream())
                .collect(Collectors.toList());

        sortTradeList(tradeList);
        return tradeList;
    }

    /**
     * Fill a sell trade
     * 
     * @param trade
     * @param customer
     * @param account
     * @param stock
     * @param quantityLeftToFill
     */
    public void fillMySellTrade(Trade trade, Customer customer, Account account, CustomStock stock,
            int quantityLeftToFill) {
        double stockBid = stock.getBid();
        double available_balance = account.getAvailable_balance();
        double balance = account.getBalance();

        trade.setStatus("filled");
        trade.setFilled_quantity(trade.getQuantity());
        stock.setLast_price(stockBid);

        account.setAvailable_balance(available_balance + (stockBid * quantityLeftToFill));
        account.setBalance(balance + (stockBid * quantityLeftToFill));

        Portfolio portfolio = customer.getPortfolio();
        deleteAsset(stock, trade, portfolio, quantityLeftToFill, "fromSell");
    }

    /**
     * Fill the corresponding buy trade
     * 
     * @param buyTrade
     * @param stock
     * @param buyList
     */
    public void fillStockBuyTrade(Trade buyTrade, CustomStock stock, List<Trade> buyList) {

        long buyTradeCustomerId = buyTrade.getCustomerId();
        Customer buyTradeCustomer = customers.findById(buyTradeCustomerId).orElse(null);

        double stockAsk = stock.getAsk();
        int quantity = buyTrade.getQuantity();

        Account buyTradeAccount = buyTrade.getAccount();
        double available_balance = buyTradeAccount.getAvailable_balance();
        double balance = buyTradeAccount.getBalance();

        buyTradeAccount.setAvailable_balance(available_balance - (stockAsk * quantity));
        buyTradeAccount.setBalance(balance - (stockAsk * quantity));
        buyTrade.setStatus("filled");
        buyTrade.setFilled_quantity(buyTrade.getQuantity());
        stock.setLast_price(stockAsk);

        Portfolio portfolio = buyTradeCustomer.getPortfolio();
        createAsset(stock, buyTrade, portfolio, quantity, "fromSell");

        // remove the first trade because it's filled
        buyList.remove(0);

        if (buyList.isEmpty()) {
            stock.setAsk_volume(0);
        } else {
            stock.setAsk(buyList.get(0).getAsk());
            stock.setAsk_volume(buyList.get(0).getQuantity());
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
     * @param tradeAsk
     * @param quantityLeftToFill
     * @param buyList
     */
    public void sellMatch(CustomStock stock, Trade trade, Account account, Customer customer, double tradeAsk,
            int quantityLeftToFill, List<Trade> buyList) {

        int quantity = trade.getQuantity();
        int prevFilledQuantity = trade.getFilled_quantity();
        double stockBid = stock.getBid();

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

        List<Trade> sortedOpenAndPartialFilledBuyTradeList = getOpenAndPartialFilledBuyTradeList(trade.getSymbol());

        if (sortedOpenAndPartialFilledBuyTradeList.isEmpty()) {
            // no more stock in the market
            stock.setBid_volume(0);
            return;
        }

        int stockBidVol = stock.getBid_volume();

        // check for quantity
        // if my trade == stock vol -> stock trade filled, my trade filled
        // else if my trade > stock vol -> stock trade filled
        // else if my trade < stock vol -> my trade filled
        // loop again

        Trade buyTrade = buyList.get(0);
        Portfolio tradeBuyPortfolio = customers.findById(buyTrade.getCustomerId()).orElse(null).getPortfolio();

        if (quantityLeftToFill == (stockBidVol - prevFilledQuantity)) {
            // sell trade is filled
            fillMySellTrade(trade, customer, account, stock, quantityLeftToFill);

            // stock trade is filled and removed from list
            fillStockBuyTrade(buyTrade, stock, buyList);

        } else if (quantityLeftToFill > (stockBidVol - prevFilledQuantity)) {

            // stock trade is filled and removed from list
            fillStockBuyTrade(buyTrade, stock, buyList);

            Portfolio portfolio = customer.getPortfolio();
            deleteAsset(stock, trade, portfolio, (stockBidVol - prevFilledQuantity), "FromSell");

            // sell trade is partial filled
            int afterFilledQuantity = trade.getFilled_quantity();
            quantityLeftToFill = quantity - afterFilledQuantity;

            sellMatch(stock, trade, account, customer, tradeAsk, quantityLeftToFill, buyList);
        } else if (quantityLeftToFill < (stockBidVol - prevFilledQuantity)) {
            // sell trade is filled
            fillMySellTrade(trade, customer, account, stock, quantityLeftToFill);

            // stock trade is partial filled
            stock.setBid_volume(stockBidVol - quantityLeftToFill);

            buyTrade.setStatus("partial-filled");
            buyTrade.setFilled_quantity(quantityLeftToFill);
            buyTrade.setAvg_price(stockBid);

            Account buyTradeAccount = buyTrade.getAccount();
            double available_balance = buyTradeAccount.getAvailable_balance();
            double balance = buyTradeAccount.getBalance();

            buyTradeAccount.setAvailable_balance(available_balance - (stockBid * quantityLeftToFill));
            buyTradeAccount.setBalance(balance - (stockBid * quantityLeftToFill));

            createAsset(stock, buyTrade, tradeBuyPortfolio, quantityLeftToFill, "fromSell");
        }

    }

    /**
     * Create a new sell trade
     * 
     * @param trade
     * @param customer
     * @param account
     * @param stock
     */
    public void sell(Trade trade, Customer customer, Account account, CustomStock stock) {
        double tradeAsk = trade.getAsk();
        double stockBid = stock.getBid();
        String symbol = trade.getSymbol();
        int quantity = trade.getQuantity();

        // if order is market sell
        if (tradeAsk == 0)
            tradeAsk = stock.getBid();

        Portfolio portfolio = customer.getPortfolio();
        long portfolioId = portfolio.getId();

        // if customer does not have the same stock in portofolio
        Asset asset = assets.findByCodeAndPortfolioId(symbol, portfolioId)
                .orElseThrow(() -> new InsufficientStockException());

        // if quantity in portfolio is lesser than trade quantity, then throw exception
        if (quantity > asset.getQuantity()) {
            throw new InsufficientStockException();
        }

        List<Trade> sortedOpenAndPartialFilledBuyTrade = getOpenAndPartialFilledBuyTradeList(symbol);

        // check if the market has the buying stocks the customer is selling
        if (!(sortedOpenAndPartialFilledBuyTrade.isEmpty())) {
            // if trade's ask is lower than stock's bid -> match & sell
            if (tradeAsk <= stockBid) {
                int quantityLeftToFill = quantity;
                // check price in previous trades (better price match first)
                sellMatch(stock, trade, account, customer, tradeAsk, quantityLeftToFill,
                        sortedOpenAndPartialFilledBuyTrade);
            } else {
                // if stocks not matched and the customer's trade is the best price
                // then update the stock with the customer's trade
                trade.setStatus("open");
                updateTradeToStock(trade, stock);
            }
        } else {
            // if stocks not matched and the customer's trade is the best price
            // then update the stock with the customer's trade
            trade.setStatus("open");
            updateTradeToStock(trade, stock);
        }

    }

    /***************************************************************************
     * OTHERS
     ***************************************************************************/

    /**
     * Sort the open and partial filled Trade list according to the ask price and
     * the time the Trade was made
     * 
     * @param tradeList
     */
    public void sortTradeList(List<Trade> tradeList) {
        Comparator<Trade> compareByAsk = Comparator.comparing(Trade::getAsk);
        Comparator<Trade> compareByDate = Comparator.comparing(Trade::getDate);
        Comparator<Trade> compareByAskAndDate = compareByAsk.thenComparing(compareByDate);

        Collections.sort(tradeList, compareByAskAndDate);
    }

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
            // if bid volume is 0
            if (stock.getBid_volume() == 0) {
                stock.setBid(trade.getBid());
                stock.setBid_volume(trade.getQuantity());
                stocks.save(stock);
            } else {
                // if this trade's bid is lower than the stock's previous ask
                // if this trade's bid is higher than the stock's previous bid
                // -> save new bid price and quantity into the stocks database
                if ((trade.getBid() < stock.getAsk()) && (trade.getBid() > stock.getBid())) {
                    stock.setBid(trade.getBid());
                    stock.setBid_volume(trade.getQuantity());
                    stocks.save(stock);
                }
            }
            // if sell order
        } else {
            // if ask volume is 0
            if (stock.getAsk_volume() == 0) {
                stock.setAsk(trade.getAsk());
                stock.setAsk_volume(trade.getQuantity());
                stocks.save(stock);
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

    }

    /**
     * Update the Trade to expire if the Trade is open or partial filled and if the
     * current time exceeds 5pm on the same day
     **/
    // second, minute, hour, day of month, month, day(s) of week
    @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "GMT+8")
    public void updateStatusToExpire() {
        List<Trade> allTradeList = trades.findAll();
        for (Trade trade : allTradeList) {
            // date with the 0 value is for the 20k inital stocks
            if (trade.getDate() != 0
                    && (trade.getStatus().equals("open") || trade.getStatus().equals("partial-filled"))) {
                trade.setStatus("expired");
            }

            long accountId = trade.getAccountId();

            Account account = accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

            // calculate price according to the stocks that did not get filled
            if (trade.getAction().equals("buy")) {
                double price = (trade.getQuantity() - trade.getFilled_quantity()) * trade.getBid();

                account.setBalance(account.getBalance() + price);
                account.setAvailable_balance(account.getAvailable_balance() + price);
            }

            trades.save(trade);
        }
    }

}