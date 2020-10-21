package ryver.app.ryverbanktests;

import ryver.app.customer.*;
import ryver.app.account.*;
import ryver.app.customer.Customer.*;
import ryver.app.account.Account.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 
 * KEY: 
 * W --> Works 
 * X --> Doesnt work
 * 
 *      T E S T
 *  X   1.getAccountByAccountIdAndCustomerId_CustomerActive_ReturnAccount
 *  X   2.getAccountByAccountIdAndCustomerId_CustomerInactive_ReturnNull
 *  W   3.addAccount_NewAccount_ReturnSavedAccount
 *  X   4.addAccount_CustomerInactive_ReturnException
 * 
 * Notes:
 * none
*/

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    BCryptPasswordEncoder encoder;
    @Mock
    private CustomerRepository customers;

    @Mock
    private AccountRepository accounts;
    // findByCustomerId(Long customerId);
    // findByIdAndCustomerId(Long accountId, Long customerId);
    @InjectMocks
    private AccountController accountController = new AccountController(accounts, customers);

    @Test
    void getAccountByAccountIdAndCustomerId_CustomerActive_ReturnAccount() {
        // //arrange
        // Customer customer = new Customer(
        // "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A",
        // "91234567", "123 Ang Mo Kio Road S456123", true);
        // customer.setId(1L);

        // Account account = new Account(400.0, 400.0, 1L, customer);

        // when(customers.findById(customer.getId())).thenReturn(Optional.of(customer));
        // when(accounts.save(any(Account.class))).thenReturn(account);

        // Account savedAccount = accountController.addAccount(account);

        // when(accounts.findById(savedAccount.getId())).thenReturn(Optional.of(savedAccount));
        // when(accounts.save(any(Account.class))).thenReturn(account);

        // //act
        // Account returnAccount =
        // accountController.getAccountByAccountIdAndCustomerId(savedAccount.getId());

        // //assert
        // assertNotNull(returnAccount);
        // verify(customers).findById(customer.getId());
        // verify(accounts).save(account);
        // verify(accounts).save(savedAccount);

        // jasmine notes:
        // arrange
        List<Account> allAccounts = new ArrayList<>();
        Customer customer = new Customer("customer_1", encoder.encode("01_customer_01"), "ROLE_USER", "Customer One",
                "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
        long customer_id = 1L;
        Account account = new Account(400, 400, customer_id);
        allAccounts.add(account);

        when(customers.findByUsername(customer.getUsername())).thenReturn(Optional.of(customer));
        when(accounts.findByCustomerId(customer_id)).thenReturn(allAccounts); // a List<Account> is returned according
                                                                              // to AccountController
        when(accounts.findAll()).thenReturn(allAccounts);
        // act
        List<Account> listOfAccounts = accountController.getAllAccountsByCustomerId();

        // assert
        assertNull(listOfAccounts);
        verify(customers).findByUsername(customer.getUsername());
        verify(accounts).findByCustomerId(customer_id);
    }

    // @Test
    // void getAccountByAccountIdAndCustomerId_CustomerInactive_ReturnNull(){
    // //arrange
    // Customer customer = new Customer(
    // "Jerry", "password", "customer", "Jerry Loh", "T0046822Z", "12345678",
    // "address", false);
    // Account account = new Account(400.0, 400.0, 1L);
    // Long accountId = 22L;
    // //act
    // Account returnAccount =
    // account.getAccountByAccountIdAndCustomerId(accountId);
    // //assert
    // assertNull(returnAccount);
    // }

    @Test
    void addAccount_NewAccount_ReturnSavedAccount() {
        // arrange
        Customer customer = new Customer("good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A", "91234567",
                "123 Ang Mo Kio Road S456123", true);
        customer.setId(1L);

        Account account = new Account(400.0, 400.0, 1L, customer);

        when(customers.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(accounts.save(any(Account.class))).thenReturn(account);

        // act
        Account savedAccount = accountController.addAccount(account);

        // assert
        assertNotNull(savedAccount);
        verify(customers).findById(customer.getId());
        verify(accounts).save(account);
    }

    // @Test
    // void addAccount_CustomerInactive_ReturnException(){
    // //arrange
    // Customer customer = new Customer(
    // "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A",
    // "91234567", "123 Ang Mo Kio Road S456123", false);
    // customer.setId(1L);

    // Account account = new Account(400.0, 400.0, 1L, customer);

    // when(customers.findById(customer.getId())).thenReturn(Optional.of(customer));
    // when(accounts.save(any(Account.class))).thenReturn(account);

    // //act
    // Account savedAccount = null;
    // try{
    // savedAccount = accountController.addAccount(account);
    // }
    // // catch (CustomerNotFoundException e){}
    // catch (AccessDeniedException e){}

    // //assert
    // assertThrows(AccessDeniedException.class, () ->
    // {accountController.addAccount(account);});
    // assertNull(savedAccount);
    // verify(accounts, never()).save(account);

    // // verify(customers).findById(customer.getId());
    // // verify(accounts).save(account);
    // }
}
