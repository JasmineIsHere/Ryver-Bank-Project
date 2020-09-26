package ryver.app.customer;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomCustomerDetailsService implements UserDetailsService {
    private CustomerRepository customers;
    
    public CustomCustomerDetailsService(CustomerRepository customers) {
        this.customers = customers;
    }
    @Override
    /** To return a UserDetails for Spring Security 
     *  Note that the method takes only a username.
        The UserDetails interface has methods to get the password.
    */
    public UserDetails loadUserByUsername(String username)  throws UsernameNotFoundException {
        return customers.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }
    
}