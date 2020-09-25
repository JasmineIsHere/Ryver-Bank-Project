package ryver.app.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository users;
    
    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }
    public UserDetails loadUserByUsername(String username)  throws UsernameNotFoundException {
        return users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }
    
}