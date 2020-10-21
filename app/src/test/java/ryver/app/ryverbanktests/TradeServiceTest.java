// package test.java.ryver.app;

// import ryver.app.customer.*;
// import ryver.app.account.*;
// import ryver.app.transaction.*;
// import ryver.app.trade.*;
// import ryver.app.customer.Customer.*;
// import ryver.app.account.Account.*;
// import ryver.app.transaction.Transaction.*;
// import ryver.app.trade.Trade.*;

// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.ApplicationContext;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// /** 
//  * KEY: 
//  * W --> Works 
//  * X --> Doesnt work
//  * 
//  *      T E S T
//  *  X   1.getAllTransactionsByAccountId_getTransactionList_returnListOfTransactions
//  *  X   2.getAllTransactionsByAccountId_none_returnException
//  *  X   3.addTransaction_newTransaction_returnSavedTransaction
//  *  X   4.addTransaction_newTransaction_returnSavedTransaction
//  * 
//  * Notes:
//  * none
// */

// @ExtendWith(MockitoExtension.class)
// public class TradeServiceTest {
//     @Mock
//     BCryptPasswordEncoder encoder;
//     @Mock
//     private CustomerRepository customers;
//     @Mock
//     private AccountRepository accounts;

//     @Mock
//     private TradeRepository trades;
//     @InjectMocks
//     private TradeController tradeController;

//     @Test
//     void getAllTransactionsByAccountId_getTransactionList_returnListOfTransactions(){
//         //arrange
//         Customer sender = new Customer(
//             "Jolene", "password", "manager", "Jolene Loh", "T0046822Z", "12345678", "address", true);
//         when(customers.save(any(Customer.class))).thenReturn(sender);
//         Account senderAccount = new Account(1000.0, 1000.0, sender.getId(), sender);
//         when(accounts.save(any(Account.class))).thenReturn(sender);
        
//         Customer sender = new Customer(
//             "Jack", "password", "manager", "Jack Tan", "T1234567Z", "22345678", "address", true);
//         when(customers.save(any(Customer.class))).thenReturn(reciever);        
//         Account recieverAccount = new Account(1000.0, 1000.0, reciever.getId(), reciever);
//         when(accounts.save(any(Account.class))).thenReturn(reciever);
        
//         Transaction transaction = new Transaction(senderAccount.getId(), recieverAccount.getId(), 100.0, senderAccount);
//         when(transactions.save(any(Transaction.class))).thenReturn(transaction);

//         //act
//         List<Transaction> transactionList = transactionController.getAllTransactionsByAccountId(senderAccount.getId());

//         //assert
//         assertNotNull(transactionList);
//         verify(customers).findByUsername("Jolene");
//         verify(customers).findByUsername("Jack");
//         verify(accounts).findById(senderAccount.getId());
//         verify(accounts).findById(recieverAccount.getId());
//         verify(transactions).findBySenderOrReciever(senderAccount.getId(), recieverAccount.getId());
//     }

//     @Test
//     void addTransaction_newTransaction_returnSavedTransaction(){
//         //arrange
//         Customer sender = new Customer(
//             "Jolene", "password", "manager", "Jolene Loh", "T0046822Z", "12345678", "address", true);
//         when(customers.save(any(Customer.class))).thenReturn(sender);
//         Account senderAccount = new Account(1000.0, 1000.0, sender.getId(), sender);
//         when(accounts.save(any(Account.class))).thenReturn(sender);
        
//         Customer sender = new Customer(
//             "Jack", "password", "manager", "Jack Tan", "T1234567Z", "22345678", "address", true);
//         when(customers.save(any(Customer.class))).thenReturn(reciever);        
//         Account recieverAccount = new Account(1000.0, 1000.0, reciever.getId(), reciever);
//         when(accounts.save(any(Account.class))).thenReturn(reciever);
        
//         Transaction transaction = new Transaction(senderAccount.getId(), recieverAccount.getId(), 100.0, senderAccount);
//         when(transactions.save(any(Transaction.class))).thenReturn(transaction);

//         //act
//         Transaction savedTransaction = transactionController.addTransactions(senderAccount.getId(), transaction);

//         //assert
//         assertNotNull(savedTransaction);
//         verify(customers).findByUsername("Jolene");
//         verify(customers).findByUsername("Jack");
//         verify(accounts).findById(senderAccount.getId());
//         verify(accounts).findById(recieverAccount.getId());
//         verify(transactions).findBySenderOrReciever(senderAccount.getId(), recieverAccount.getId());
//         verify(transactions).save(transaction);
//     }
// }