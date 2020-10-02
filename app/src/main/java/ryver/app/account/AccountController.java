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

@RestController
public class AccountController {
    private AccountRepository accounts;
    private CustomerRepository customers;

    public AccountController(AccountRepository accounts, CustomerRepository customers){
        this.accounts = accounts;
        this.customers = customers;
    }
    
    // Authentication for ROLE_MANAGER or ROLE_USER to access his own accounts
    // Deactivated customer returns 403 forbidden
    @PreAuthorize("hasRole('MANAGER') || #customerId == authentication.principal.id")
    @GetMapping("/customers/{customerId}/accounts")
    public List<Account> getAllAccountsByCustomerId(@PathVariable (value = "customerId") Long customerId) {
        Customer customer = customers.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // if customer is deactivated, return 403 forbidden
        if (!customer.isActive()) {
            throw new AccessDeniedException("403 returned");
        }

        return accounts.findByCustomerId(customerId);
    }

    // Deactivated customer returns 403 forbidden
    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/customers/{customerId}/accounts/{accountId}")
    public Account getAccountByAccountIdAndCustomerId(@PathVariable (value = "accountId") Long accountId, 
        @PathVariable (value = "customerId") Long customerId) {
        
        Customer customer = customers.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // if customer is deactivated, return 403 forbidden
        if (!customer.isActive()) {
            throw new AccessDeniedException("403 returned");
        }

        return accounts.findByIdAndCustomerId(accountId, customerId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/customers/{customerId}/accounts")
    public Account addAccount (@PathVariable (value = "customerId") Long customerId, @Valid @RequestBody Account account) {

        Customer customer = customers.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // if customer is deactivated, return 403 forbidden
        if (!customer.isActive()) {
            throw new AccessDeniedException("403 returned");
        }

        account.setCustomer(customer);
        return accounts.save(account);

        
    }

}