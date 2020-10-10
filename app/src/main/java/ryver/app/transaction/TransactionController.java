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
    
    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/accounts/{accountId}/transactions")
    public List<Transaction> getAllTransactionsByAccountId(@PathVariable (value = "accountId") Long accountId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();

        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        Long customerId = customer.getId();

        Account account =  accounts.findByIdAndCustomerId(accountId, customerId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));

        // return transactions.findByAccountId(accountId);
        return transactions.findBySenderOrReceiver(accountId, accountId);
        
    }

    // @GetMapping("/customers/{customerId}/accounts/{accountId}")
    // public Account getAccountByAccountIdAndCustomerId(@PathVariable (value = "accountId") Long accountId, 
    //     @PathVariable (value = "customerId") Long customerId) {
        
    //     if(!customers.existsById(customerId)) {
    //         throw new CustomerNotFoundException(customerId);
    //     }
    //     return accounts.findByIdAndCustomerId(accountId, customerId).orElseThrow(() -> new AccountNotFoundException(accountId));
    // }

    @PostMapping("/accounts/{accountId}/transactions")
    public Transaction addTransaction (@PathVariable (value = "accountId") Long accountId, @Valid @RequestBody Transaction transaction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        if (accountId != transaction.getSender()) {
            throw new AccountMismatchException();
        }

        if(transaction.getAmount() <= 0){
            throw new BadBalanceException();
        }
        Customer customer = customers.findByUsername(customerUsername) //user that log in and the sender
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        //sender
        Long customerId = customer.getId();

        Account senderAccount =  accounts.findByIdAndCustomerId(accountId, customerId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
        
            //receiver
        Long receiverAccountId = transaction.getReceiver();

        Account receiverAccount =  accounts.findById(receiverAccountId)
            .orElseThrow(() -> new AccountNotFoundException(receiverAccountId));

        if (senderAccount.getAvailable_balance() < transaction.getAmount()){
            throw new InsufficientBalanceException();
        } 
            
        senderAccount.setBalance(senderAccount.getAvailable_balance() - transaction.getAmount());
        senderAccount.setAvailable_balance(senderAccount.getAvailable_balance() - transaction.getAmount());

        receiverAccount.setBalance(receiverAccount.getAvailable_balance() + transaction.getAmount());
        receiverAccount.setAvailable_balance(receiverAccount.getAvailable_balance() + transaction.getAmount());
        
        transaction.setAccount(senderAccount);
        return transactions.save(transaction);


        // return customers.findById(customerId).map(customer ->{
        //     account.setCustomer(customer);
        //     return accounts.save(account);
        // }).orElseThrow(() -> new CustomerNotFoundException(customerId));
    }















    // MANY TO MANY STUFF
    // // Deactivated customer returns 403 forbidden
    // @PreAuthorize("authentication.principal.active == true")
    // @GetMapping("/accounts/{accountId}/transactions")
    // public List<Transaction> getAllTransactionsByAccountIdAndCustomerId(@PathVariable (value = "accountId") Long accountId) {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     String customerUsername = authentication.getName();

    //     Customer customer = customers.findByUsername(customerUsername)
    //         .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

    //     Long customerId = customer.getId();

    //     Account account =  accounts.findByIdAndCustomerId(accountId, customerId)
    //         .orElseThrow(() -> new AccountNotFoundException(accountId));

    //     List<Transaction> transaction = transactions.findByAccountId(accountId);

    //     for (Transaction t : transaction) {
    //         System.out.println(t.getAccount());

    //         // account is garbage collected?!?!?!?!
    //     }

    //     System.out.println(transaction);

    //     return transaction;
    //     // return transactions.findByAccountIdAndCustomerId(accountId, customerId);
        
    // }

    // // Deactivated customer returns 403 forbidden
    // @PreAuthorize("authentication.principal.active == true")
    // @ResponseStatus(HttpStatus.CREATED)
    // @PostMapping("/accounts/{accountId}/transactions")
    // public Transaction addTransaction (@PathVariable (value = "accountId") Long accountId, @Valid @RequestBody Transaction transaction) {
        
    //     Set<Account> accountSet = new HashSet<>();

    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     //will always be the sender that post a transfer request
        
    //     String customerUsername = authentication.getName(); // good_user_1

    //     //System.out.println(customerUsername);
    //     //SENDER STUFF
    //     Customer customer = customers.findByUsername(customerUsername)
    //         .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

    //     Long customerId = customer.getId();

    //     Account senderAccount =  accounts.findByIdAndCustomerId(accountId, customerId)
    //         .orElseThrow(() -> new AccountNotFoundException(accountId));

    //     //RECEIVER STUFF
    //     Long receiverAccountId = transaction.getReceiver();

    //     Account receiverAccount =  accounts.findById(receiverAccountId)
    //         .orElseThrow(() -> new AccountNotFoundException(receiverAccountId));

    //     //System.out.println("receiverAcc = " + accounts.findById(receiverAccountId));

    //     senderAccount.setBalance(senderAccount.getAvailable_balance() - transaction.getAmount());
    //     senderAccount.setAvailable_balance(senderAccount.getAvailable_balance() - transaction.getAmount());

    //     receiverAccount.setBalance(receiverAccount.getAvailable_balance() + transaction.getAmount());
    //     receiverAccount.setAvailable_balance(receiverAccount.getAvailable_balance() + transaction.getAmount());
 
    //     //something wrong here
    //     accountSet.add(senderAccount);
    //     accountSet.add(receiverAccount);
    //     // problem is with the accounts ^^^^ when second posting of transaction

    //     transaction.setAccount(accountSet);

    //     return transactions.save(transaction);

    // }

}