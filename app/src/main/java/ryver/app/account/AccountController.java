package ryver.app.account;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;
import ryver.app.customer.CustomerNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@RestController
public class AccountController {
    private AccountRepository accounts;
    private CustomerRepository customers;

    public AccountController(AccountRepository accounts, CustomerRepository customers){
        this.accounts = accounts;
        this.customers = customers;
    }
    
  
    // Deactivated customer returns 403 forbidden
    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/accounts")
    public List<Account> getAllAccountsByCustomerId() {
        //source:https://www.baeldung.com/get-user-in-spring-security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customer_name = authentication.getName();
        
        Customer customer = customers.findByUsername(customer_name)
            .orElseThrow(() -> new CustomerNotFoundException(customer_name));

        long customerId = customer.getId();
        // if customer is deactivated, return 403 forbidden
        if (!customer.isActive()) {
            throw new AccessDeniedException("403 returned");
        }
        
        if (customer.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))){
            return accounts.findAll();
        } else{
            return accounts.findByCustomerId(customerId);
        }
        
    }

    // Deactivated customer returns 403 forbidden
    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/accounts/{accountId}")
    public Account getAccountByAccountIdAndCustomerId(@PathVariable (value = "accountId") Long accountId) {
        //source:https://www.baeldung.com/get-user-in-spring-security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customer_name = authentication.getName();
        
        Customer customer = customers.findByUsername(customer_name)
            .orElseThrow(() -> new CustomerNotFoundException(customer_name));

        long customerId = customer.getId();
        // if customer is deactivated, return 403 forbidden
        if (!customer.isActive()) {
            throw new AccessDeniedException("403 returned");
        }

        return accounts.findByIdAndCustomerId(accountId, customerId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/accounts")
    public Account addAccount (@Valid @RequestBody Account account) {
        Customer customer = customers.findById(account.getCustomer_id())
            .orElseThrow(() -> new CustomerNotFoundException(account.getCustomer_id()));

        //if customer is deactivated, return 403 forbidden
        if (!customer.isActive()) {
            throw new AccessDeniedException("403 returned");
        }

        account.setCustomer(customer);
        return accounts.save(account);

        
    }

}