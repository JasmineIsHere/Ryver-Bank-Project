package ryver.app.customer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ryver.app.account.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

/* Implementations of UserDetails to provide user information to Spring Security, 
e.g., what authorities (roles) are granted to the customer and whether the account is enabled or not
*/
public class Customer implements UserDetails{
    private static final long serialVersionUID = 1L;

    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @NotNull(message = "Username should not be null")
    @Size(min = 5, max = 20, message = "Username should be between 5 and 20 characters")
    private String username;
    
    @NotNull(message = "Password should not be null")
    @Size(min = 8, message = "Password should be at least 8 characters. ")
    private String password;

    @NotNull(message = "Authorities should not be null")
    // We define two roles/authorities: ROLE_USER, ROLE_MANAGER or ROLE_ANALYST
    private String authorities;

    @NotNull(message = "Full Name should not be null")
    @Size(min = 5, max = 20, message = "Username should be between 5 and 20 characters. ")
    private String fullName;

    
    @NotNull(message = "NRIC should not be null")
    @Size(min = 9, max = 9, message = "NRIC must be valid. ")
    private String nric;

    @NotNull(message = "Phone Number should not be null")
    @Pattern(regexp = "[689][0-9]{7}", message = "Phone number must be valid. ")
    private String phone;

    @NotNull(message = "Address should not be null")
    @Size(min = 5, max = 200, message = "Address should be between 5 and 200 characters. ")
    private String address;

    private boolean active;


    @OneToMany(mappedBy = "customer",
    orphanRemoval = true,
    cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Account> accounts;

    public Customer(String username, String password, String authorities, String fullName, String nric, String phone, String address, boolean active){
        this.username = username;
        this.password = password;
        this.authorities = authorities;

        this.fullName = fullName;
        this.nric = nric;
        this.phone = phone;
        this.address = address;
        this.active = active;
    }


    /* Return a collection of authorities granted to the user.
    */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority(authorities));
    }

    /*
    The various is___Expired() methods return a boolean to indicate whether
    or not the user’s account is enabled or expired.
    */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
        public boolean isAccountNonLocked() {
    return true; 
    }
    @Override
        public boolean isCredentialsNonExpired() {
    return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
}