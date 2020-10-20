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

import ryver.app.stock.CustomStock;
import ryver.app.stock.StockRepository;
import ryver.app.transaction.InsufficientBalanceException;
import ryver.app.stock.InvalidStockException;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@RestController
public class TradeController {
    private TradeRepository trades;
    private CustomerRepository customers;
    private AccountRepository accounts;
    private StockRepository stocks;
    
   
    public TradeController(TradeRepository trades, CustomerRepository customers, AccountRepository accounts, StockRepository stocks){
        this.trades = trades;
        this.customers = customers;
        this.accounts = accounts;
        this.stocks = stocks;
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

        Trade trade = trades.findByIdAndCustomerId(customerId, tradeId)
            .orElseThrow(() -> new TradeNotFoundException(tradeId));

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
    public void updateStatusToExpire(Trade trade) {
        ZonedDateTime current = ZonedDateTime.now();
        int currentHour = current.getHour();

        if (currentHour >= 17 && (trade.getStatus() == "open" || trade.getStatus() == "partial-filled")) {
            trade.setStatus("expired");
        }

        trades.save(trade);

    }

    public void buyTradeCheckForSellMatch(CustomStock stock, Trade trade, Account account, double calculatedBuyPrice) {
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
            checkTradeQuantityAgainstStockAskVol(stock, trade, account, tradeSellListOfSymbol, tradeQuantity);
        } else {
            // if bid is lower than stock's ask
            System.out.println("AA");
            trade.setStatus("open");
            updateTradeToStock(trade, stock);
            // if there's sufficient balance, set available balance to new balance
            account.setAvailable_balance(available_balance - calculatedBuyPrice);
        }
    }

    public void checkTradeQuantityAgainstStockAskVol(CustomStock stock, Trade trade, Account account, List<Trade> tradeSellListOfSymbol, long tradeQuantity) {
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
            System.out.println("W");
            trade.setStatus("filled");
            trade.setAvg_price(stockAsk);
            stock.setLast_price(new BigDecimal(stockAsk, MathContext.DECIMAL64));

            account.setAvailable_balance(available_balance - (stockAsk * tradeQuantity));
            account.setBalance(balance - (stockAsk * tradeQuantity));

            // if the stock fills the trade just nice, revert the stock back to the previous stock
            if (tradeQuantity == stockAskVol) {
                System.out.println("X");
                tradeSellListOfSymbol.get(0).setStatus("filled");

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

            tradeSellListOfSymbol.get(0).setStatus("filled");
            System.out.println("Before: " + tradeSellListOfSymbol);
            
            // remove that trade from the list if its filled
            tradeSellListOfSymbol.remove(0);
            System.out.println("After: " + tradeSellListOfSymbol);

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


                    if (stockAsk == tradeSellListOfSymbol.get(j).getAsk()) {
                        tradeQuantity -= stockAskVol; 
                        // check for quantity.
                        checkTradeQuantityAgainstStockAskVol(stock, trade, account, tradeSellListOfSymbol, tradeQuantity);
                    } else {

                        // set trade
                        trade.setStatus("partial-filled");
                        trade.setAvg_price(stockAsk);
                        trade.setFilled_quantity((int)stockAskVol);

                        // set account
                        // available balance - stock bought - leftover open stocks
                        account.setAvailable_balance(available_balance - (stockAsk * stockAskVol) - (stock.getAsk().doubleValue() * (tradeQuantity - stockAskVol)));
                        account.setBalance(balance - (stockAsk * stockAskVol));

                        // if the current stock's ask price is not the same as the next ask price then break
                        break;
                    }
                }
            } else {
                // set trade
                trade.setStatus("partial-filled");
                trade.setAvg_price(stockAsk);
                trade.setFilled_quantity((int)stockAskVol);

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
            List<Trade> specificStockOpenSellTrade = getSpecificStockOpenAndPartialFilledSellTrade(symbol);

            // check if the market has the stocks the customer is buying
            if (!(specificStockOpenSellTrade.isEmpty())) {
                System.out.println("R");
                // check price in previous trades (better price match first)
                buyTradeCheckForSellMatch(stock, trade, account, calculatedBuyPrice);


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

            // if it's a sell market order, change the ask value to current stock's bid price
            if (ask == 0.0) {
                ask = stock.getBid().doubleValue();
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

        Trade trade = trades.findByIdAndCustomerId(customerId, tradeId)
            .orElseThrow(() -> new TradeNotFoundException(tradeId));


        // customer can cancel a trade if its open
        if (trade.getStatus() == "open" && updatedTradeInfo.getStatus() == "cancelled") {
            trade.setStatus("cancelled");
        }

        trades.save(trade);
        return trade;
    }
}