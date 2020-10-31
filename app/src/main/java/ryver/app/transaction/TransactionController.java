package ryver.app.transaction;

import java.util.*;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;
import ryver.app.customer.CustomerNotFoundException;

import ryver.app.account.Account;
import ryver.app.account.AccountRepository;
import ryver.app.account.AccountNotFoundException;
import ryver.app.account.AccountMismatchException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class TransactionController {
    private AccountRepository accounts;
    private TransactionRepository transactions;
    private CustomerRepository customers;

    public TransactionController(AccountRepository accounts, TransactionRepository transactions,
            CustomerRepository customers) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.customers = customers;
    }

    @GetMapping("/api/accounts/{accountId}/transactions")
    public List<Transaction> getAllTransactionsByAccountId(@PathVariable(value = "accountId") Long accountId) {

        // user that was authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();

        // user that was authenticated
        Customer customer = customers.findByUsername(customerUsername)
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));


        Long customerId = customer.getId();

        /* 
         * try to find an account that has match specified account and user that was authenticated
         * 
         * if there is no match, either (1) the account does not exist
         *  or (2) the account does not belong to the authenticated user 
        */
        //case 1
        accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        //case 2
        accounts.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountMismatchException());

        // return transactions.findByAccountId(accountId);
        return transactions.findByToOrFrom(accountId, accountId);

    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/accounts/{accountId}/transactions")
    public Transaction addTransaction(@PathVariable(value = "accountId") Long accountId,
            @Valid @RequestBody Transaction transaction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();

        // account accessed in the URL should be the same as the account used to sent money
        if (accountId != transaction.getFrom()) {
            throw new AccountMismatchException();
        }

        // transaction amount should be more than 0
        if (transaction.getAmount() <= 0) {
            throw new BadBalanceException();
        }

        Customer customer = customers.findByUsername(customerUsername) // user that log in and the sender
                .orElseThrow(() -> new CustomerNotFoundException(customerUsername));

        // sender
        Long customerId = customer.getId();

        Account senderAccount = accounts.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // receiver
        Long receiverAccountId = transaction.getTo();

        Account receiverAccount = accounts.findById(receiverAccountId)
                .orElseThrow(() -> new AccountNotFoundException(receiverAccountId));

        //sender has not enough money to transfer
        if (senderAccount.getAvailable_balance() < transaction.getAmount()) {
            throw new InsufficientBalanceException();
        }

        // updating the balance and available balance for sender and receiver accounts
        senderAccount.setBalance(senderAccount.getAvailable_balance() - transaction.getAmount());
        senderAccount.setAvailable_balance(senderAccount.getAvailable_balance() - transaction.getAmount());

        receiverAccount.setBalance(receiverAccount.getAvailable_balance() + transaction.getAmount());
        receiverAccount.setAvailable_balance(receiverAccount.getAvailable_balance() + transaction.getAmount());

        transaction.setAccount(senderAccount);
        return transactions.save(transaction);
    }

}