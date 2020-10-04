package ryver.app.transaction;

import java.util.*;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;
import ryver.app.customer.CustomerNotFoundException;

import ryver.app.account.Account;
import ryver.app.account.AccountRepository;
import ryver.app.account.AccountNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@RestController
public class TransactionController {
    private AccountRepository accounts;
    private TransactionRepository transactions;
    private CustomerRepository customers;

    public TransactionController(AccountRepository accounts, TransactionRepository transactions, CustomerRepository customers){
        this.accounts = accounts;
        this.transactions = transactions;
        this.customers = customers;
    }
    
    // Deactivated customer returns 403 forbidden
    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/accounts/{accountId}/transactions")
    public List<Transaction> getAllTransactionsByAccountIdAndCustomerId(@PathVariable (value = "accountId") Long accountId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();

        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        Long customerId = customer.getId();

        Account account =  accounts.findByIdAndCustomerId(accountId, customerId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));

        List<Transaction> transaction = transactions.findByAccountId(accountId);

        for (Transaction t : transaction) {
            System.out.println(t.getAccount());

            // account is garbage collected?!?!?!?!
        }

        System.out.println(transaction);

        return transaction;
        // return transactions.findByAccountIdAndCustomerId(accountId, customerId);
        
    }

    // Deactivated customer returns 403 forbidden
    @PreAuthorize("authentication.principal.active == true")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/accounts/{accountId}/transactions")
    public Transaction addTransaction (@PathVariable (value = "accountId") Long accountId, @Valid @RequestBody Transaction transaction) {
        
        Set<Account> accountSet = new HashSet<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        String customerUsername = authentication.getName(); // good_user_1

        // System.out.println(customerUsername);

        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        Long customerId = customer.getId();

        Account senderAccount =  accounts.findByIdAndCustomerId(accountId, customerId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));

        Long receiverAccountId = transaction.getReceiver();

        Account receiverAccount =  accounts.findById(receiverAccountId)
            .orElseThrow(() -> new AccountNotFoundException(receiverAccountId));

        // System.out.println(accounts.findById(account.getId()));

        accountSet.add(senderAccount);
        accountSet.add(receiverAccount);
        // problem is with the accounts ^^^^ when second posting of transaction

        transaction.setAccount(accountSet);

        return transactions.save(transaction);

    }

}