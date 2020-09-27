package ryver.app.transaction;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import ryver.app.customer.CustomerRepository;
import ryver.app.customer.CustomerNotFoundException;

import ryver.app.account.AccountRepository;
import ryver.app.account.AccountNotFoundException;

@RestController
public class TransactionController {
    private AccountRepository accounts;
    private TransactionRepository transactions;
    // private CustomerRepository customers;

    public TransactionController(AccountRepository accounts, TransactionRepository transactions){
        this.accounts = accounts;
        this.transactions = transactions;
        // this.customers = customers;
    }
    
    // Authentication for ROLE_USER to access his own transactions
    // @PreAuthorize("#customerId == authentication.principal.id")
    @GetMapping("/customers/{customerId}/accounts/{accountId}/transactions")
    public List<Transaction> getAllTransactionsByAccountIdAndCustomerId(@PathVariable (value = "accountId") Long accountId) {

        // if(!customers.existsById(customerId)) {
        //     throw new CustomerNotFoundException(customerId);
        // }

        if(!accounts.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        // return transactions.findByAccountId(accountId);
        return transactions.findByAccountId(accountId);
        
    }

    // @GetMapping("/customers/{customerId}/accounts/{accountId}")
    // public Account getAccountByAccountIdAndCustomerId(@PathVariable (value = "accountId") Long accountId, 
    //     @PathVariable (value = "customerId") Long customerId) {
        
    //     if(!customers.existsById(customerId)) {
    //         throw new CustomerNotFoundException(customerId);
    //     }
    //     return accounts.findByIdAndCustomerId(accountId, customerId).orElseThrow(() -> new AccountNotFoundException(accountId));
    // }

    @PreAuthorize("#customerId == authentication.principal.id")
    @PostMapping("/customers/{customerId}/accounts/{accountId}/transactions")
    public Transaction addTransaction (@PathVariable (value = "customerId") Long customerId, @PathVariable (value = "accountId") Long accountId, @Valid @RequestBody Transaction transaction) {
        return accounts.findById(accountId).map(account ->{
            transaction.setAccount(account);
            return transactions.save(transaction);
        }).orElseThrow(() -> new AccountNotFoundException(accountId));


        // return customers.findById(customerId).map(customer ->{
        //     account.setCustomer(customer);
        //     return accounts.save(account);
        // }).orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

}