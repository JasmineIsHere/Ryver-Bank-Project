package ryver.app.trade;

import java.util.List;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import ryver.app.stock.InvalidStockException;
import ryver.app.stock.StockRepository;
import ryver.app.account.Account;
import ryver.app.account.AccountRepository;
import ryver.app.customer.Customer;
import ryver.app.customer.CustomerNotFoundException;
import ryver.app.customer.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@RestController
public class TradeController {
    private TradeRepository trades;
    private StockRepository stocks;
    private CustomerRepository customers;
    private AccountRepository accounts;

    public TradeController(TradeRepository trades, StockRepository stocks,CustomerRepository customers, AccountRepository accounts){
        this.trades = trades;
        this.stocks = stocks;
        this.customers = customers;
        this.accounts = accounts;
    }
    
  
    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/trade/{id}")
    public Trade getTradeByTradeId(@PathVariable (value = "id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return trades.findById(id)
            .orElseThrow(() -> new TradeNotFoundException(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/trade")
    public Trade newTrade(@Valid@RequestBody Trade trade){
        Customer customer = customers.findById(trade.getCustomer_id())
            .orElseThrow(() -> new CustomerNotFoundException(trade.getCustomer_id()));
        Account account = accounts.findById(trade.getAccount_id())
            .orElseThrow(() -> new ryver.app.account.AccountNotFoundException(trade.getAccount_id()));
        
        //account dont belong to user
        if(account.getCustomer_id() != trade.getCustomer_id())
            throw new InvalidUserAccountException();

        //if customer is deactivated, return 403 forbidden
        if (!customer.isActive()) {
            throw new AccessDeniedException("403 returned");
        }

        //if no such stock
        if(stocks.findBySymbol(trade.getSymbol()) == null)
            throw new InvalidStockException(trade.getSymbol());

        //if buying check if user have sufficient fund
        //if selling check if user have sufficient stock

        //if all good
        return trades.save(trade);
    }

}