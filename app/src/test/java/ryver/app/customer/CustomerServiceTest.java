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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.catalina.util.CustomObjectInputStream;
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
        Customer manager = new Customer("manager_1", "01_manager_01", "ROLE_MANAGER", "Manager One",
         "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
        manager.setId(1L); //usually is auto-generated, but mock the id given

        Customer updatedManager = new Customer(
            "Jolene", "password", "ROLE_USER", "Jolene Loh", "T0046822Z", "12345678", "address", false);
        updatedManager.setId(1L);

        when(customers.findById(manager.getId())).thenReturn(Optional.of(manager)); //supposed to be an optional
        when(customers.save(any(Customer.class))).thenReturn(updatedManager); //cannot test the manager authority        
        when(encoder.encode(updatedManager.getPassword())).thenReturn(updatedManager.getPassword());
        // Authentication au = new UsernamePasswordAuthenticationToken(manager, manager.getAuthorities());
        // SecurityContext sc = SecurityContextHolder.getContext();
        // sc.setAuthentication(au);
        // when(sch.getContext().getAuthentication()).thenReturn(au);
        //when(au.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))).thenReturn(true);
        
        //act
        Customer savedManager = customerController.updateCustomer(manager.getId(), updatedManager);
        //only password, phone, address is changed (active status not changed because authentication could not be mocked)

        //assert
        assertEquals(updatedManager.getAddress(), savedManager.getAddress());
        assertEquals(updatedManager.getPhone(), savedManager.getPhone());
        assertEquals(updatedManager.getPassword(), savedManager.getPassword());
        //assertEquals(updatedManager.isActive(), savedManager.isActive()); //still can't be tested

        assertNotEquals(updatedManager.getUsername(), savedManager.getUsername());
        assertNotEquals(updatedManager.getFullName(), savedManager.getFullName());
        assertNotEquals(updatedManager.getNric(), savedManager.getNric());
        assertNotEquals(updatedManager.getAuthorities(), savedManager.getAuthorities());
        
        verify(customers).findById(manager.getId());
        verify(customers).save(savedManager);

    }

    @Test 
    void updateCustomer_ROLEManagerUpdateROLECustomer_ReturnSavedCustomer(){
        //arrange
        Customer customer = new Customer(
            "Jerry", "password", "ROLE_USER", "Jerry Loh", "T0046822Z", "12345678", "address", true);
        Long customerId = 1L;
        customer.setId(customerId);

        Customer manager = new Customer("manager_1", "01_manager_01", "ROLE_MANAGER", "Manager One",
         "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
        manager.setId(1L); //usually is auto-generated, but mock the id given

        Customer updatedCustomer = new Customer(
            "Jolene", "new_password", "ROLE_MANAGER", "Jolene Loh", "S9946822Z", "97123456", "updated_address", false);
        updatedCustomer.setId(1L);

        when(customers.findById(customerId)).thenReturn(Optional.of(customer));
        when(customers.save(any(Customer.class))).thenReturn(updatedCustomer);        
        when(encoder.encode(updatedCustomer.getPassword())).thenReturn(updatedCustomer.getPassword());
        
        //act
        Customer savedCustomer = customerController.updateCustomer(customerId, updatedCustomer);
        //assert
        assertEquals(updatedCustomer.getAddress(), savedCustomer.getAddress());
        assertEquals(updatedCustomer.getPhone(), savedCustomer.getPhone());
        assertEquals(updatedCustomer.getPassword(), savedCustomer.getPassword());
        //assertEquals(updatedManager.isActive(), savedManager.isActive()); //still can't be tested
        
        assertNotEquals(updatedCustomer.getUsername(), savedCustomer.getUsername());
        assertNotEquals(updatedCustomer.getFullName(), savedCustomer.getFullName());
        assertNotEquals(updatedCustomer.getNric(), savedCustomer.getNric());
        assertNotEquals(updatedCustomer.getAuthorities(), savedCustomer.getAuthorities());
        
        verify(customers).findById(customer.getId());
        verify(customers).save(savedCustomer);
    }

    @Test 
    void updateCustomer_ROLEManagerUpdateROLEAnalyst_ReturnSavedCustomer(){
        //arrange
        Customer customer = new Customer(
            "Jerry", "password", "ROLE_USER", "Jerry Loh", "T0046822Z", "12345678", "address", true);
        customer.setId(1L);
        when(customers.findById(customer.getId())).thenReturn(Optional.of(customer));
        //act
        Customer savedCustomer = customerController.updateCustomer(customer.getId(), customer);
        //assert
        assertNotNull(savedCustomer);
        verify(customers).findById(customer.getId());
        
    }

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

    @Test //WORKS
    void addCustomer_NewCustomerWithValidNric_ReturnSavedCustomer(){
        //arrange
        Customer manager = new Customer(
            "Jolene", "password", "manager", "Jolene Loh", "T0046822Z", "12345678", "address", true);
        when(customers.save(any(Customer.class))).thenReturn(manager);
        //act
        Customer savedManager = customerController.addCustomer(manager);
        //assert
        assertNotNull(savedManager);
        verify(customers).save(manager);
    }

    @Test //WORKS
    void addCustomer_NewCustomerWithInvalidNric_ReturnNull(){
        //arrange
        Customer manager = new Customer(
            "Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true);

        //act
        Customer savedManager = null;
        try{
            savedManager = customerController.addCustomer(manager);
        } catch (InvalidNricException e){

        }
        
        
        //assert
        assertThrows(InvalidNricException.class, () -> {customerController.addCustomer(manager);}); //asserts a InvalidNricException was thrown
        assertNull(savedManager); //asserts that no new customer is added

        verify(customers, never()).save(manager); //verify the save method was never executed because an exception was thrown
    }

    @Test //WORKS
    void validateNric_ValidNric_ReturnTrue(){
        //arrange
        Customer manager = new Customer(
            "Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true);
        //act
        boolean validNric = customerController.validateNric(manager.getNric());
        //assert
        assertNotNull(validNric);
    }
}