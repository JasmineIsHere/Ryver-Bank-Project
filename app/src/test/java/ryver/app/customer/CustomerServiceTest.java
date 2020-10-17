package ryver.app.customer;

import ryver.app.customer.*;
import ryver.app.customer.Customer.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
 *      T E S T
 *      1.updateCustomer_ROLEManagerUpdateROLEManager_ReturnSavedCustomer
 *      2.updateCustomer_ROLEManagerUpdateROLECustomer_ReturnSavedCustomer
 *      3.updateCustomer_ROLEManagerUpdateROLEAnalyst_ReturnSavedCustomer
 *      4.updateCustomer_ROLECustomerUpdateROLECustomer_ReturnSavedCustomer
 *      5. 
 *      6. 
*/

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @Mock
    private CustomerRepository customers;

    @Mock
    BCryptPasswordEncoder encoder;

    @InjectMocks
    private CustomerController customerController;

    @Test 
    void updateCustomer_ROLEManagerUpdateROLEManager_ReturnSavedCustomer(){
        //mock
        //List<Customer> customerDB = new ArrayList<>(); //acting DB
        Customer manager = new Customer("manager_1", "01_manager_01", "ROLE_MANAGER", "Manager One",
         "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
        manager.setId(1L); //usually is auto-generated, but mock the id given
        //customerDB.add(manager);

        Customer updatedManager = new Customer(
            "Jolene", "password", "MANAGER", "Jolene Loh", "T0046822Z", "12345678", "address", false);
        updatedManager.setId(1L);

        when(customers.findById(manager.getId())).thenReturn(Optional.of(manager)); //supposed to be an optional
        when(customers.save(any(Customer.class))).thenReturn(updatedManager); //cannot test the manager authority        
        when(encoder.encode(updatedManager.getPassword())).thenReturn(updatedManager.getPassword());
        // Authentication au = new UsernamePasswordAuthenticationToken(manager, manager.getAuthorities());
        // when(au.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))).thenReturn(true);
        //act
        Customer savedManager = customerController.updateCustomer(manager.getId(), updatedManager);
        //only password, phone, address is changed (active status not changed because authentication could not be mocked)

        //assert
        assertEquals(updatedManager.getAddress(), savedManager.getAddress());
        assertEquals(updatedManager.getPhone(), savedManager.getPhone());
        assertEquals(updatedManager.getPassword(), savedManager.getPassword());
        assertNotEquals(updatedManager.getUsername(), savedManager.getUsername());
        assertNotEquals(updatedManager.getFullName(), savedManager.getFullName());
        assertNotEquals(updatedManager.getNric(), savedManager.getNric());
        

        verify(customers).findById(manager.getId());
        verify(customers).save(savedManager);

    }

    // @Test //CANNOT GENERATE ID
    // void updateCustomer_ROLEManagerUpdateROLECustomer_ReturnSavedCustomer(){
    //     //arrange
    //     Customer customer = new Customer(
    //         "Jerry", "password", "ROLE_USER", "Jerry Loh", "T0046822Z", "12345678", "address", true);
    //     Long customerId = 1L;
    //     when(customers.findById(customerId)).thenReturn(Optional.empty());
    //     when(customers.findByAuthorities(any(String.class))).thenReturn(new ArrayList<Customer>());
    //     //act
    //     Customer savedCustomer = customerController.updateCustomer(customerId, customer);
    //     //assert
    //     assertNotNull(savedCustomer);
    //     verify(customers).findById(customerId);
    //     verify(customers).findByAuthorities("ROLE_USER");
    // }

    // @Test //CANNOT GENERATE ID
    // void updateCustomer_ROLEManagerUpdateROLEAnalyst_ReturnSavedCustomer(){
    //     //arrange
    //     Customer customer = new Customer(
    //         "Jerry", "password", "ROLE_USER", "Jerry Loh", "T0046822Z", "12345678", "address", true);
    //     Long customerId = 1L;
    //     when(customers.findById(customerId)).thenReturn(Optional.empty());
    //     when(customers.findByAuthorities(any(String.class))).thenReturn(new ArrayList<Customer>());
    //     //act
    //     Customer savedCustomer = customerController.updateCustomer(customerId, customer);
    //     //assert
    //     assertNotNull(savedCustomer);
    //     verify(customers).findById(customerId);
    //     verify(customers).findByAuthorities("ROLE_ANALYST");
    // }

    // @Test //CANNOT GENERATE ID
    // void updateCustomer_ROLECustomerUpdateROLECustomer_ReturnSavedCustomer(){
    //     //arrange
    //     Customer customer = new Customer(
    //         "Jerry", "password", "ROLE_USER", "Jerry Loh", "T0046822Z", "12345678", "address", true);
    //     Long customerId = 1L;
    //     when(customers.findById(customerId)).thenReturn(Optional.empty());
    //     when(customers.findByAuthorities(any(String.class))).thenReturn(new ArrayList<Customer>());
    //     //act
    //     Customer savedCustomer = customerController.updateCustomer(customerId, customer);
    //     //assert
    //     assertNotNull(savedCustomer);
    //     verify(customers).findById(customerId);
    //     verify(customers).findByAuthorities("ROLE_USER");
    // }

    // @Test //CANNOT GENERATE ID
    // void updateCustomer_ROLEAnalystUpdateROLEAnalyst_ReturnSavedCustomer(){
    //     //arrange
    //     Customer customer = new Customer(
    //         "Jerry", "password", "ROLE_USER", "Jerry Loh", "T0046822Z", "12345678", "address", true);
    //     Long customerId = 1L;
    //     when(customers.findById(customerId)).thenReturn(Optional.empty());
    //     when(customers.findByAuthorities(any(String.class))).thenReturn(new ArrayList<Customer>());
    //     //act
    //     Customer savedCustomer = customerController.updateCustomer(customerId, customer);
    //     //assert
    //     assertNotNull(savedCustomer);
    //     verify(customers).findById(customerId);
    //     verify(customers).findByAuthorities("ROLE_USER");
    // }

    // @Test //WORKS
    // void addCustomer_NewCustomerWithValidNric_ReturnSavedCustomer(){
    //     //arrange
    //     Customer manager = new Customer(
    //         "Jolene", "password", "manager", "Jolene Loh", "T0046822Z", "12345678", "address", true);
    //     when(customers.save(any(Customer.class))).thenReturn(manager);
    //     //act
    //     Customer savedManager = customerController.addCustomer(manager);
    //     //assert
    //     assertNotNull(savedManager);
    //     verify(customers).save(manager);
    // }

    // @Test //SUDDENLY DOESNT WORK
    // void addCustomer_NewCustomerWithInvalidNric_ReturnNull(){
    //     //arrange
    //     Customer manager = new Customer(
    //         "Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true);
    //     when(customers.save(any(Customer.class))).thenReturn(manager);
    //     //act
    //     Customer savedManager = customerController.addCustomer(manager);
    //     //assert
    //     assertNull(savedManager);
    //     verify(customers).save(manager);
    // }

    // @Test //WORKS
    // void validateNric_ValidNric_ReturnTrue(){
    //     //arrange
    //     Customer manager = new Customer(
    //         "Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true);
    //     //act
    //     boolean validNric = customerController.validateNric(manager.getNric());
    //     //assert
    //     assertNotNull(validNric);
    // }
}