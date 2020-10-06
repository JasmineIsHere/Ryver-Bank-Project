// package ryver.app;

// import ryver.app.*;
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
//     void updateCustomer_ROLEManager_ReturnSavedCustomer(){
//         //arrange
//         Customer manager = new Customer(
//             "Jolene", "password", "manager", "Jolene Loh", "T0046822Z", "12345678", "address", true);
//         Long customerId = 12L;
//         when(customers.findById(customerId)).thenReturn(Optional.empty());
//         when(customers.findByAuthorities(any(String.class))).thenReturn(new List<Customer>());
//         //act
//         Customer savedManager = customerController.updateCustomer(customerId, manager);
//         //assert
//         assertNotNull(savedManager);
//         verify(customers).findById(customerId);
//         verify(customers).findByAuthorities(manager.getAuthorities());
//     }

//     @Test
//     void updateCustomer_notROLEManager_ReturnSavedCustomer(){
//         //arrange
//         Customer customer = new Customer(
//             "Jerry", "password", "customer", "Jerry Loh", "T0046822Z", "12345678", "address", true);
//         Long customerId = 10L;
//         when(customers.findById(customerId)).thenReturn(Optional.empty());
//         when(customers.findByAuthorities(any(String.class))).thenReturn(new List<Customer>());
//         //act
//         Customer savedCustomer = customerController.updateCustomer(customerId, customer);
//         //assert
//         assertNotNull(savedCustomer);
//         verify(customers).findById(customerId);
//         verify(customers).findByAuthorities(customer.getAuthorities());
//     }

//     @Test
//     void addCustomer_NewCustomerWithValidNric_ReturnSavedCustomer(){
//         //arrange
//         Customer manager = new Customer(
//             "Jolene", "password", "manager", "Jolene Loh", "T0046822Z", "12345678", "address", true);
//         when(customers.save(any(Customer.class))).thenReturn(manager);
//         //act
//         Customer savedManager = customerController.addCustomer(manager);
//         //assert
//         assertNotNull(savedManager)
//         verify(customers).save(manager);
//     }

//     @Test
//     void addCustomer_NewCustomerWithInvalidNric_ReturnNull(){
//         //arrange
//         Customer manager = new Customer(
            // "Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true);
//             when(customers.save(any(Customer.class))).thenReturn(manager);
//         //act
//         Customer savedManager = customerController.addCustomer(manager);
//         //assert
//         assertNull(savedManager);
//         verify(customers).save(manager);
//     }

//     @Test
//     void validateNric_ValidNric_ReturnTrue(){
//         //arrange
//         Customer manager = new Customer(
//             "Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true);
//         //act
//         boolean validNric = validateNric(manager.getNric());
//         //assert
//         assertTrue(validNric);
//     }

//     @Test
//     void validateNric_InvalidNric_ReturnFalse(){
//         //arrange
//         Customer customer = new Customer(
//             "Jerry", "password", "customer", "Jerry Loh", "T0046822Z", "12345678", "address", true);
//         //act
//         boolean invalidNric = validateNric(customer.getNric());
//        //assert
//         assertFalse(invalidNric);
//     }
// }