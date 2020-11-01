package ryver.app.account;

import ryver.app.customer.*;

import java.util.List;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class AccountController {
    // Repositories
    private AccountRepository accounts;
    private CustomerRepository customers;

    public AccountController(AccountRepository accounts, CustomerRepository customers) {
        this.accounts = accounts;
        this.customers = customers;
    }

    /**
     * Get a list of all Accounts associated with the logged in Customer's ID If the
     * user is a manager, get all existing accounts Valid customer - Returns 200 OK
     * Deactivated customer - Returns 403 Forbidden
     * Returns 200 OK (if no exceptions)
     * 
     * @return List<Account>
     */
    @GetMapping("/api/accounts")
    public List<Account> getAllAccountsByCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();

        Customer customer = customers.findByUsername(customerUsername)
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        if (!customer.isActive()) {
            throw new InactiveCustomerException();
        }
        
        long customerId = customer.getId();

        // If ROLE_MANAGER -> get all the accounts
        // Else -> get own accounts
        if (customer.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            return accounts.findAll();
        } else {
            return accounts.findByCustomerId(customerId);
        }
    }

    /**
     * Get a specific Account associated with the logged Customer's ID that has the specified AccountId 
     * Valid customer - Returns 200 OK 
     * Deactivated customer - Returns 403 Forbidden
     * Returns 200 OK (if no exceptions)
     * 
     * @param accountId
     * @return Account
     */
    @GetMapping("/api/accounts/{accountId}")
    public Account getAccountByAccountIdAndCustomerId(@PathVariable(value = "accountId") Long accountId) {
        // source: https://www.baeldung.com/get-user-in-spring-security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();

        Customer customer = customers.findByUsername(customerUsername)
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        if (!customer.isActive()) {
            throw new InactiveCustomerException();
        }

        long customerId = customer.getId();

        return accounts.findByIdAndCustomerId(accountId, customerId).orElseThrow(() -> new AccountMismatchException());
    }

    /**
     * Create a new Account using the JSON data 
     * Valid customer - Returns 201 Created
     * Deactivated customer - Returns 403 Forbidden
     * Returns 201 Created (if no exceptions)
     * 
     * @param account
     * @return Account
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/accounts")
    public Account addAccount(@Valid @RequestBody Account account) {
        Customer customer = customers.findById(account.getCustomer_id())
                .orElseThrow(() -> new CustomerNotFoundException(account.getCustomer_id()));

        // If customer is deactivated, return 403 forbidden
        if (!customer.isActive()) {
            throw new InactiveCustomerException();
        }

        account.setCustomer(customer);
        account.setAvailable_balance(account.getBalance());
        return accounts.save(account);
    }

}