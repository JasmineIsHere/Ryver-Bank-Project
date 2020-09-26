package ryver.app.account;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import ryver.app.customer.CustomerRepository;
import ryver.app.customer.CustomerNotFoundException;

@RestController
public class AccountController {
    private AccountRepository accounts;
    private CustomerRepository customers;

    public AccountController(AccountRepository accounts, CustomerRepository customers){
        this.accounts = accounts;
        this.customers = customers;
    }
    
    // Authentication for ROLE_ADMIN or ROLE_USER to access his own accounts
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    @GetMapping("/customers/{customerId}/accounts")
    public List<Account> getAllAccountsByCustomerId(@PathVariable (value = "customerId") Long customerId) {
        if(!customers.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        return accounts.findByCustomerId(customerId);
    }

    @GetMapping("/customers/{customerId}/accounts/{accountId}")
    public Account getAccountByAccountIdAndCustomerId(@PathVariable (value = "accountId") Long accountId, 
        @PathVariable (value = "customerId") Long customerId) {
        
        if(!customers.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        return accounts.findByIdAndCustomerId(accountId, customerId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @PostMapping("/customers/{customerId}/accounts")
    public Account addAccount (@PathVariable (value = "customerId") Long customerId, @Valid @RequestBody Account account) {
        return customers.findById(customerId).map(customer ->{
            account.setCustomer(customer);
            return accounts.save(account);
        }).orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

}