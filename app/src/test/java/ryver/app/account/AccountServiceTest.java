// package ryver.app;

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

//     @InjectMocks
//     private AccountController accountController;

//     @Test
//     void getAllAccountsByCustomerId_ROLECustomer_ReturnListOfAccounts(){
//         //arrange
//         Account acc = new Account();
//         //act
//         //assert
//     }
//     @Test
//     void getAllAccountsByCustomerId_ROLEManager_ReturnListOfAccounts(){
//         //arrange
//         //act
//         //assert
//     }
//     @Test
//     void getAllAccountsByCustomerId_CustomerInactive_ReturnListOfAccounts(){
//         //arrange
//         //act
//         //assert
//     }
//     @Test
//     void getAccountByAccountIdAndCustomerId_CustomerActive_ReturnAccount(){
//         //arrange
//         //act
//         //assert
//     }
//     @Test
//     void getAccountByAccountIdAndCustomerId_CustomerInactive_ReturnAccount(){
//         //arrange
//         //act
//         //assert
//     }
//     @Test
//     void addAccount_NewAccount_ReturnSavedAccount(){
//         //arrange
//         //act
//         //assert
//     }
// }