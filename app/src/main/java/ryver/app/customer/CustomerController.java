package ryver.app.customer;

import java.util.List;

import javax.validation.Valid;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.http.HttpStatus;

@RestController
public class CustomerController {
    private CustomerRepository customers;
    private BCryptPasswordEncoder encoder;

    public CustomerController(CustomerRepository customers, BCryptPasswordEncoder encoder){
        this.customers = customers;
        this.encoder = encoder;
    }

    @GetMapping("/customers")
    public List<Customer> getCustomers() {
        return customers.findByAuthorities("ROLE_USER");
    }

    // Managers can update customers information, customer can update OWN information (phone, address, password)
    @PreAuthorize("hasRole('MANAGER') or #customerId == authentication.principal.id")
    @PutMapping("/customers/{customerId}")
    public Customer updateCustomer(@PathVariable (value = "customerId") Long customerId, @Valid @RequestBody Customer updatedCustomerInfo){
        
        Customer customer = customers.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // fields which customers and managers can update
        customer.setPassword(encoder.encode(updatedCustomerInfo.getPassword()));
        customer.setPhone(updatedCustomerInfo.getPhone());
        customer.setAddress(updatedCustomerInfo.getAddress());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // fields which only managers can update
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            customer.setUsername(updatedCustomerInfo.getUsername());
            customer.setFullName(updatedCustomerInfo.getFullName());
            customer.setNric(updatedCustomerInfo.getNric());
            customer.setActive(updatedCustomerInfo.getActive());
        }

        return customers.save(customer);
    }

    /**
    * Using BCrypt encoder to encrypt the password for storage 
    * @param customer
     * @return
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/customers")
    public Customer addCustomer(@Valid @RequestBody Customer customer){
        customer.setPassword(encoder.encode(customer.getPassword()));
        return customers.save(customer);
    }
   
}