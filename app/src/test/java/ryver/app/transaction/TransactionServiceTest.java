// package test.java.ryver.app;

// import ryver.app.customer.*;
// import ryver.app.account.*;
// import ryver.app.customer.Customer.*;
// import ryver.app.account.Account.*;

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

// @ExtendWith(MockitoExtension.class)
// public class TransactionServiceTest {
//     @Mock
//     BCryptPasswordEncoder encoder;
//     @Mock
//     private CustomerRepository customers;
//     @Mock
//     private AccountRepository accounts;

//     @Mock
//     private TransactionRepository transactions;
//     // List<Transaction> findByAccountId(Long accountId);
//     // List<Transaction> findBySenderOrReceiver(Long accountId, Long accountId2);
//     @InjectMocks
//     private TransactionController transactionController;
//     // public List<Transaction> getAllTransactionsByAccountId(@PathVariable (value = "accountId") Long accountId) {
//     // public Transaction addTransaction (@PathVariable (value = "accountId") Long accountId, @Valid @RequestBody Transaction transaction) {

//     @Test
//     void getAllTransactionsByAccountId_getTransactionList_returnListOfTransactions(){
//         //arrange
//         Customer manager = new Customer(
//             "Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true);
//         when(customers.save(any(Customer.class))).thenReturn(manager);
//         Account account = new Account();
//         when(accounts.save(any(Account.class))).thenReturn(account);
//         Transaction transaction = new Transaction();
//         when(transactions.save(any(Transaction.class))).thenReturn(transaction);
//         //act
//         List<Transaction> transactionList = transactionController.getAllTransactionsByAccountId(account.getId());
//         //assert
//         assertNotNull(transactionList);
//         verify(customers).save(manager);
//         verify(accounts).save(account);
//         verify(transactions).save(transaction);
//     }

//     @Test
//     void addTransaction_newTransaction_returnSavedTransaction(){
//         //arrange
//         Customer manager = new Customer(
//             "Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true);
//         when(customers.save(any(Customer.class))).thenReturn(manager);
//         Account account = new Account();
//         when(accounts.save(any(Account.class))).thenReturn(account);
//         Transaction transaction = new Transaction();
//         when(transactions.save(any(Transaction.class))).thenReturn(transaction);
//         //act
//         Transaction savedTransaction = transactionController.addTransactions(account.getId(), transaction);
//         //assert
//         assertNotNull(savedTransaction);
//         verify(customers).save(manager);
//         verify(accounts).save(account);
//         verify(transactions).save(transaction);
//     }

//     @Test
//     void addTransaction_invalidAccountId_returnException(){
//         //arrange
//         //act
//         String message = "You cannot access this account";
//         //assert
//         assertEquals(message, );
//     }

//     @Test
//     void addTransaction_badBalance_returnException(){
//         //arrange
//         //act
//         String message = "Bad balance detected";
//         //assert
//         assertEquals(message, );
//     }

//     @Test
//     void addTransaction_insufficientBalance_returnException(){
//         //arrange
//         //act
//         String message = "Insufficient Balance";
//         //assert
//         assertEquals(message, );
//     }
// }