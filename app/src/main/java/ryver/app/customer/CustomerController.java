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

    // Managers can update (active or not active) customers information (phone, address, password, active)
    // Active customer can update OWN information (phone, address, password)
    // Deactivated customer cannot update OWN information
    @PreAuthorize("authentication.principal.active == true and (hasRole('MANAGER') or #customerId == authentication.principal.id)")
    @PutMapping("/customers/{customerId}")
    public Customer updateCustomer(@PathVariable (value = "customerId") Long customerId, @Valid @RequestBody Customer updatedCustomerInfo){
        
        Customer customer = customers.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));


        // fields which customers and managers can update - password, phone, address
        customer.setPassword(encoder.encode(updatedCustomerInfo.getPassword()));
        customer.setPhone(updatedCustomerInfo.getPhone());
        customer.setAddress(updatedCustomerInfo.getAddress());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // fields which only managers can update - active
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            customer.setActive(updatedCustomerInfo.isActive());
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
        if (!validateNric(customer.getNric()))
            throw new InvalidNricException();

        return customers.save(customer);
    }

    public static boolean validateNric(String nric){
        int total = 0;
        int arr[] = {2,7,6,5,4,3,2};
        char charArr[] = nric.toCharArray();
        
        if(nric.length()-2 != arr.length)
            return false;
        
            for(int i=0; i<arr.length; i++){
                total += arr[i] * Character.getNumericValue(charArr[i+1]);
            }
        
        if(charArr[0] == 'T' || charArr[0] == 'G')
            total += 4;
        
        if(charArr[0] == 'S' || charArr[0] == 'T'){
            switch(total%11) {
            case 0:
              if(charArr[charArr.length-1] == 'J')
                  return true;
              break;
            case 1:
              if(charArr[charArr.length-1] == 'Z')
                  return true;
              break;
            case 2:
              if(charArr[charArr.length-1] == 'I')
                  return true;
              break;
            case 3:
              if(charArr[charArr.length-1] == 'H')
                  return true;
              break;
            case 4:
              if(charArr[charArr.length-1] == 'G')
                  return true;
              break;
            case 5:
              if(charArr[charArr.length-1] == 'F')
                  return true;
              break;
            case 6:
              if(charArr[charArr.length-1] == 'E')
                  return true;
              break;
            case 7:
              if(charArr[charArr.length-1] == 'D')
                  return true;
              break;
            case 8:
              if(charArr[charArr.length-1] == 'C')
                  return true;
              break;
            case 9:
              if(charArr[charArr.length-1] == 'B')
                  return true;
              break;
            case 10:
              if(charArr[charArr.length-1] == 'A')
                  return true;
              break;
            default:
              return false;
          }
        }else if(charArr[0] == 'F' || charArr[0] == 'G'){
            switch(total%11) {
            case 0:
              if(charArr[charArr.length-1] == 'X')
                  return true;
              break;
            case 1:
              if(charArr[charArr.length-1] == 'W')
                  return true;
              break;
            case 2:
              if(charArr[charArr.length-1] == 'U')
                  return true;
              break;
            case 3:
              if(charArr[charArr.length-1] == 'T')
                  return true;
              break;
            case 4:
              if(charArr[charArr.length-1] == 'R')
                  return true;
              break;
            case 5:
              if(charArr[charArr.length-1] == 'Q')
                  return true;
              break;
            case 6:
              if(charArr[charArr.length-1] == 'P')
                  return true;
              break;
            case 7:
              if(charArr[charArr.length-1] == 'N')
                  return true;
              break;
            case 8:
              if(charArr[charArr.length-1] == 'M')
                  return true;
              break;
            case 9:
              if(charArr[charArr.length-1] == 'L')
                  return true;
              break;
            case 10:
              if(charArr[charArr.length-1] == 'K')
                  return true;
              break;
            default:
              return false;
            }
        }
        return false;
       }
   
}