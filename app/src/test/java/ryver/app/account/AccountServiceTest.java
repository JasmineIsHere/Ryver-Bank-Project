package test.java.ryver.app;

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

    // @Test
    // void getAllAccountsByCustomerId_ROLECustomer_ReturnListOfAccounts(){
    //     //arrange
    //     Customer customer = new Customer(
    //         "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
    //     customer.setId(1L);
    //     Account accountOne = new Account(400, 400, 1L, customer);
    //     // Account accountTwo = new Account(500, 500, 1L, customer);

    //     when(customers.findById(customer.getId())).thenReturn(Optional.of(customer)); 
    //     when(accounts.save(any(Account.class))).thenReturn(accountOne);

    //     //act
    //     Account listOfAccounts = accountController.getAllAccountsByCustomerId(1L); 

    //     //assert
    //     assertNotNull(listOfAccounts);
    //     verify(customers).findByAuthorities("ROLE_USER");
    // }

    // @Test
    // void getAccountByAccountIdAndCustomerId_CustomerActive_ReturnAccount(){
    //     //arrange
    //     Customer manager = new Customer(
    //     "Jolene", "password", "manager", "Jolene Loh", "T0046822Z", "12345678", "address", true);
    //     Account account = new Account(1L, 400.0, 400.0);
    //     Long accountId = 22L;
    //     //act
    //     Account returnAccount = account.getAccountByAccountIdAndCustomerId(accountId);
    //     //assert
    //     assertNotNull(returnAccount);
    // }

    // @Test
    // void getAccountByAccountIdAndCustomerId_CustomerInactive_ReturnNull(){
    //     //arrange
    //     Customer customer = new Customer(
    //         "Jerry", "password", "customer", "Jerry Loh", "T0046822Z", "12345678", "address", false);
    //     Account account = new Account(400.0, 400.0, 1L);
    //     Long accountId = 22L;
    //     //act
    //     Account returnAccount = account.getAccountByAccountIdAndCustomerId(accountId);
    //     //assert
    //     assertNull(returnAccount);
    // }

    @Test
    void addAccount_NewAccount_ReturnSavedAccount(){
        //arrange
        Customer customer = new Customer(
            "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
        customer.setId(1L);
        Account account = new Account(400.0, 400.0, 1L, customer);
        when(customers.findById(customer.getId())).thenReturn(Optional.of(customer)); 
        when(accounts.save(any(Account.class))).thenReturn(account);
    
        //act
        Account savedAccount = accountController.addAccount(account);

        //assert
        assertNotNull(savedAccount);
        verify(customers).findById(customer.getId());
        verify(accounts).save(account);
    }
}