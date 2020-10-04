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
// public class CustomerServiceTest {
//     @Mock
//     private CustomerRepository customers;

//     @InjectMocks
//     private CustomerController customerController;

//     @Test
//     // void getCustomers__ReturnListOfCustomers(){}
//     void updateCustomer_ROLEManager_ReturnSavedCustomer(){}
//     void updateCustomer_notROLEManager_ReturnSavedCustomer(){}
//     void addCustomer_NewCustomerValidNric_ReturnSavedCustomer(){}
//     void addCustomer_NewCustomerInvalidNric_ReturnSavedCustomer(){}
//     void validateNric_ValidNric_ReturnTrue(){}
//     void validateNric_InvalidNric_ReturnFalse(){}
// }