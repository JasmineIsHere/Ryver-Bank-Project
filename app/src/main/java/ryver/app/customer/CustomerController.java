package ryver.app.customer;

import java.util.List;

import javax.validation.Valid;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    /**
    * Using BCrypt encoder to encrypt the password for storage 
    * @param customer
     * @return
     */
    @PostMapping("/customers")
    public Customer addCustomer(@Valid @RequestBody Customer customer){
        customer.setPassword(encoder.encode(customer.getPassword()));
        return customers.save(customer);
    }
   
}