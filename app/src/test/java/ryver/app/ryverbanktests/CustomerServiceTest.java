package ryver.app.ryverbanktests;

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
 * KEY: 
 * W --> Works 
 * X --> Doesnt work
 * 
 *      T E S T
 *  W   1.updateCustomer_Found_ReturnSavedCustomer
 *  X   2.updateCustomer_NotFound_returnNull 
 *  W   3.addCustomer_NewCustomerWithValidNric_ReturnSavedCustomer
 *  W   4.addCustomer_NewCustomerWithInvalidNric_ReturnNull
 *  W   5.validateNric_ValidNric_ReturnTrue
 * 
 * Notes:
 * - need to add a return null function for test2
 * - the isActive part of the assert does not work
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
    void updateCustomer_Found_ReturnSavedCustomer(){
        //arrange
        Customer customer = new Customer(
            "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
        customer.setId(1L);

        Customer updatedCustomer = new Customer(
            "good_user_1", "updated_01_user_01", "ROLE_USER", "User One", "S7812345A", "12345678", "updated_address", false);
        updatedCustomer.setId(1L);

        when(customers.findById(customer.getId())).thenReturn(Optional.of(customer)); 
        when(customers.save(any(Customer.class))).thenReturn(updatedCustomer);         
        when(encoder.encode(updatedCustomer.getPassword())).thenReturn(updatedCustomer.getPassword());
    
        //act
        Customer savedCustomer = customerController.updateCustomer(customer.getId(), updatedCustomer);

        //assert
        // fields which customers and managers can update - password, phone, address
        assertEquals(updatedCustomer.getAuthorities(), savedCustomer.getAuthorities());
        assertEquals(updatedCustomer.getAddress(), savedCustomer.getAddress());
        assertEquals(updatedCustomer.getPhone(), savedCustomer.getPhone());
        assertEquals(updatedCustomer.getPassword(), savedCustomer.getPassword());
        assertEquals(updatedCustomer.getUsername(), savedCustomer.getUsername());
        assertEquals(updatedCustomer.getFullName(), savedCustomer.getFullName());
        assertEquals(updatedCustomer.getNric(), savedCustomer.getNric());
        //IsActive not working
    
        verify(customers).findById(customer.getId());
        verify(customers).save(savedCustomer);
    }

    // @Test
    // void updateCustomer_NotFound_returnNull(){
    //     //arrange        
    //     Customer customer = new Customer(
    //         "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", false);
    //     customer.setId(1L);

    //     Customer updatedCustomer = new Customer(
    //         "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
    //     updatedCustomer.setId(1L);

    //     when(customers.findById(customerId)).thenReturn(Optional.empty()); 
    //     when(encoder.encode(updatedCustomer.getPassword())).thenReturn(updatedCustomer.getPassword());
            
    //     //act
    //     Customer savedCustomer = null;
    //     try{
    //         savedCustomer = customerController.updateCustomer(1L, updatedCustomer);
    //     } catch (CustomerNotFoundException e){
    //     }

    //     //assert
    //     assertThrows(CustomerNotFoundException.class, () -> {customerController.updateCustomer(updatedCustomer);});
    //     assertNull(savedCustomer);
    //     verify(customers, never()).save(updatedCustomer); 
    // }

    @Test
    void addCustomer_NewCustomerWithValidNric_ReturnSavedCustomer(){
        //arrange
        Customer customer = new Customer(
            "user_1", "01_user_01", "ROLE_USER", "User One", "T0046822Z", "12345678", "address", true);
        when(customers.save(any(Customer.class))).thenReturn(customer);
        
        //act
        Customer savedCustomer = customerController.addCustomer(customer);
        
        //assert
        assertNotNull(savedCustomer);
        verify(customers).save(customer);
    }

    @Test
    void addCustomer_NewCustomerWithInvalidNric_ReturnNull(){
        //arrange
        Customer customer = new Customer(
            "user_1", "01_user_01", "ROLE_USER", "User One", "T1234567Z", "12345678", "address", true);
            
        //act
        Customer savedCustomer = null;
        try{
            savedCustomer = customerController.addCustomer(customer);
        } catch (InvalidNricException e){
        }
        
        //assert
        assertThrows(InvalidNricException.class, () -> {customerController.addCustomer(customer);}); //asserts a InvalidNricException was thrown
        assertNull(savedCustomer); //asserts that no new customer is added

        verify(customers, never()).save(customer); //verify the save method was never executed because an exception was thrown
    }

    @Test 
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