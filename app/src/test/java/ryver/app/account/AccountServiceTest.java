// package test.java.ryver.app;

// import org.springframework.boot.test.context.SpringBootTest;

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
// public class AccountServiceTest {
//     @Mock
//     private AccountRepository accounts;
//     // findByCustomerId(Long customerId);
//     // findByIdAndCustomerId(Long accountId, Long customerId);


//     @InjectMocks
//     private AccountController accountController;

//     //what is the setup for accounts?
//     // @Test
//     // void getAllAccountsByCustomerId_ROLECustomer_ReturnListOfAccounts(){
//     //     //arrange
//     //     Account account = new Account();
//     //     //act
//     //     Account listOfAccounts = 
//     //     //assert
//     // }
//     // @Test
//     // void getAllAccountsByCustomerId_ROLEManager_ReturnListOfAccounts(){
//     //     //arrange
//     //     //act
//     //     //assert
//     // }
//     // @Test
//     // void getAllAccountsByCustomerId_CustomerInactive_ReturnListOfAccounts(){
//     //     //arrange
//     //     //act
//     //     //assert
//     // }

//     @Test
//     void getAccountByAccountIdAndCustomerId_CustomerActive_ReturnAccount(){
//         //arrange
//         Customer manager = new Customer(
//         "Jolene", "password", "manager", "Jolene Loh", "T0046822Z", "12345678", "address", true);
//         Account account = new Account();
//         Long accountId = 22L;
//         //act
//         Account returnAccount = account.getAccountByAccountIdAndCustomerId(accountId);
//         //assert
//         assertNotNull(returnAccount);
//     }

//     @Test
//     void getAccountByAccountIdAndCustomerId_CustomerInactive_ReturnNull(){
//         //arrange
//         Customer customer = new Customer(
//             "Jerry", "password", "customer", "Jerry Loh", "T0046822Z", "12345678", "address", false);
//         Account account = new Account();
//         Long accountId = 22L;
//         //act
//         Account returnAccount = account.getAccountByAccountIdAndCustomerId(accountId);
//         //assert
//         assertNull(returnAccount);
//     }

//     @Test
//     void addAccount_NewAccount_ReturnSavedAccount(){
//         //arrange
//         Customer manager = new Customer(
//             "Jolene", "password", "manager", "Jolene Loh", "T0046822Z", "12345678", "address", true);
//         Account account = new Account();
//         when(books.findByIdAndCustomerId(any(String.class))).thenReturn(new ArrayList<Account>());
//         when(accounts.save(any(Account.class))).thenReturn(account);
//         //act
//         Account savedAccount = customer.addAccount(account);
//         //assert
//         assertNotNull(savedAccount);
//         verify(accounts).findByIdAndCustomerId(manager.getAccountId(), manager.getCustomerId());
//         verify(accounts).save(account);
//     }
// }