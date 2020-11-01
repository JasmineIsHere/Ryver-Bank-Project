package ryver.app.transaction;

import ryver.app.account.*;
import ryver.app.customer.*;

import java.util.*;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class TransactionController {
    // Repositories
    private AccountRepository accounts;
    private TransactionRepository transactions;
    private CustomerRepository customers;

    public TransactionController(AccountRepository accounts, TransactionRepository transactions,
            CustomerRepository customers) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.customers = customers;
    }

    /**
     * Get a List of Transactions based on the specified accountId 
     * Returns 200 OK (if no exceptions)
     * 
     * @param accountId
     * @return List<Transaction>
     */
    @GetMapping("/api/accounts/{accountId}/transactions")
    public List<Transaction> getAllTransactionsByAccountId(@PathVariable(value = "accountId") Long accountId) {

        // User that was authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();

        // User that was authenticated
        Customer customer = customers.findByUsername(customerUsername)
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
                
        if (!customer.isActive()) {
            throw new InactiveCustomerException();
        }

        Long customerId = customer.getId();

        /*
         * Try to find an Account that matches the specified Account and User that was
         * authenticated 
         * If there is no match, either 
         * (1) the account does not exist or
         * (2) the account does not belong to the authenticated user
         */
        // Case 1
        accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        // Case 2
        accounts.findByIdAndCustomerId(accountId, customerId).orElseThrow(() -> new AccountMismatchException());

        return transactions.findByToOrFrom(accountId, accountId);

    }

    /**
     * Create a new Transaction with the specified accountId Based on JSON data
     * Returns 201 Created (if no exceptions)
     * 
     * @param accountId
     * @param transaction
     * @return Transaction
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/accounts/{accountId}/transactions")
    public Transaction addTransaction(@PathVariable(value = "accountId") Long accountId,
            @Valid @RequestBody Transaction transaction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();

        // Account accessed in the URL should be the same as the account used to sent
        if (accountId != transaction.getFrom()) {
            throw new AccountMismatchException();
        }

        // Transaction amount should be more than 0
        if (transaction.getAmount() <= 0) {
            throw new BadBalanceException();
        }

        Customer customer = customers.findByUsername(customerUsername) // User that is logged in and the sender
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        if (!customer.isActive()) {
            throw new InactiveCustomerException();
        }
        // sender
        Long customerId = customer.getId();

        Account senderAccount = accounts.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // Receiver
        Long receiverAccountId = transaction.getTo();

        Account receiverAccount = accounts.findById(receiverAccountId)
                .orElseThrow(() -> new AccountNotFoundException(receiverAccountId));

        // Sender has not enough money to transfer
        if (senderAccount.getAvailable_balance() < transaction.getAmount()) {
            throw new InsufficientBalanceException();
        }

        // Updating the balance and available balance for sender and receiver accounts
        senderAccount.setBalance(senderAccount.getAvailable_balance() - transaction.getAmount());
        senderAccount.setAvailable_balance(senderAccount.getAvailable_balance() - transaction.getAmount());

        receiverAccount.setBalance(receiverAccount.getAvailable_balance() + transaction.getAmount());
        receiverAccount.setAvailable_balance(receiverAccount.getAvailable_balance() + transaction.getAmount());

        transaction.setAccount(senderAccount);
        return transactions.save(transaction);
    }

}