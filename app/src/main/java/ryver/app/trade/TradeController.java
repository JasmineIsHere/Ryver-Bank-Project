package ryver.app.trade;

import java.sql.Timestamp;
import java.util.*;
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

    // @PreAuthorize("authentication.principal.active == true")
    // @GetMapping("/trades")
    // public List<Trade> getAllTrades(){

    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     String customerUsername = authentication.getName();
        
    //     Customer customer = customers.findByUsername(customerUsername)
    //         .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
    //     long customerId = customer.getId();

    //     return trades.findByCustomerId(customerId);

    // }

    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/trades/{tradeId}")
    public Trade getSpecificTrade(@PathVariable (value = "tradeId") Long tradeId){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
        long customerId = customer.getId();

        return trades.findByIdAndCustomerId(customerId, tradeId);

    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/trades")
    public Trade createTrade(@Valid @RequestBody Trade trade){
        
        // check if logged in user == customerId
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
        long loggedInCustomerId = customer.getId();

        if (loggedInCustomerId != trade.getCustomerId()) {
            throw new CustomerMismatchException();
        }

        // check if account belong to customer
        long accountId = trade.getAccountId();

        List<Account> loggedInAccountList = accounts.findByCustomerId(loggedInCustomerId);

        if (loggedInAccountList.isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }

        boolean accountMatched = false;
        for (Account loggedInAcc : loggedInAccountList) {
            if (loggedInAcc.getId() == accountId) {
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
        if (action != "buy" && action != "sell") {
            throw new InvalidTradeException();
        }

        double bid = trade.getBid();
        double ask = trade.getAsk();
        double calculatedBuyPrice;

        if (action == "buy") {
            // FOR BUYING
            if (bid != 0.0) {
                // Limit Order (buy)
                calculatedBuyPrice = bid * quantity;

            } else {
                // Market Order (buy) -> calculated by market ask price
                CustomStock stock = stocks.findBySymbol(symbol);
                // doubleValue() -> change Big Decimal to double
                calculatedBuyPrice = stock.getAsk().doubleValue() * quantity;

            }
            // check if account has sufficient balance
            Account account = accounts.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

            double available_balance = account.getAvailable_balance();

            if (calculatedBuyPrice > available_balance) {
                throw new InsufficientBalanceException();
            }

            // if there's sufficient balance, set available balance to new balance
            account.setAvailable_balance(available_balance - calculatedBuyPrice);
            
        } else {
            // FOR SELLING
            if (ask != 0.0) {
                // Limit Order (sell)

            } else {
                // Market Order (sell)

            }
        }
        
        


        // limit order -> customer specify price (sell -> ask price, buy -> bid price) (expire at 5)
        // market order -> market price (ask and bid -> 0.0) (done immediately)

        // better price match first
        // if same price, then match earliest trade
        
        
        
        // FOR BUYING
        // check if symbol exist in stock
        // check if enough amount in account balance when submitting the order (must be sufficient, if not error 400)
        // check if amount is enough in balance when filling the order bc bid/ ask price will change (if not enough, partial filled)
        // add to portfolio

            // buy at market price -> market ask price
            // buy at limit bid price

        // FOR SELLING
        // check if symbol exist in portfolio (quantity selling > quantity own)

            // sell at market price -> market bid price


        // set current timestamp to date
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        trade.setDate(timestamp.getTime());
        System.out.println(timestamp);

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

        Trade trade = trades.findByIdAndCustomerId(customerId, tradeId);


        // customer can cancel a trade if its open
        if (trade.getStatus() == "open" && updatedTradeInfo.getStatus() == "cancel") {
            trade.setStatus("cancel");
        }

        trades.save(trade);
        return trade;
    }
}