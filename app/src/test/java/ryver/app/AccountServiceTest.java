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
//     void getAllAccountsByCustomerId_ROLECustomer_ReturnListOfAccounts(){}
//     void getAllAccountsByCustomerId_ROLEManager_ReturnListOfAccounts(){}
//     void getAllAccountsByCustomerId_CustomerInactive_ReturnListOfAccounts(){}
//     void getAccountByAccountIdAndCustomerId_CustomerActive_ReturnAccount(){}
//     void getAccountByAccountIdAndCustomerId_CustomerInactive_ReturnAccount(){}
//     void addAccount_NewAccount_ReturnSavedAccount(){}
// }