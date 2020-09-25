// package csd.week5.user;
package ryver.app.user;
import java.util.Arrays;
import java.util.Collection;
import java.util.*;
import javax.persistence.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.*;
import ryver.app.account.*;


@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

/* Implementations of UserDetails to provide user information to Spring Security, 
e.g., what authorities (roles) are granted to the user and whether the account is enabled or not
*/
public class User implements UserDetails{

    private static final long serialVersionUID = 1L;

    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long UID;
    
    @NotNull(message = "Username should not be null")
    @Size(min = 5, max = 20, message = "Username should be between 5 and 20 characters")
    private String username;
    
    @NotNull(message = "Password should not be null")
    @Size(min = 8, message = "Password should be at least 8 characters")
    private String password;

    
    public User(String username, String password, String authorities){
        this.username = username;
        this.password = password;
        // this.fullName = fullName;
        // this.nric = nric;
        // this.phone = phone;
        // this.address = address;
        this.authorities = authorities;
        // this.isActive = 1; // true (set to 1)
    }

    
    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Account> accounts;
    // @NotNull(message = "Full Name should not be null")
    // @Size(min = 5, max = 20, message = "Username should be between 5 and 20 characters")
    // private String fullName;

    // @NotNull(message = "NRIC should not be null")
    // @Size(min = 9, max = 9, message = "NRIC should be 9 characters")
    // private String nric;

    // @NotNull(message = "Phone Number should not be null")
    // @Size(min = 8, max = 8, message = "Phone Number should be between 8 characters")
    // private String phone;

    // @NotNull(message = "Address should not be null")
    // @Size(min = 5, max = 200, message = "Username should be between 5 and 200 characters")
    // private String address;

    // private int isActive;

    @NotNull(message = "Authorities should not be null")
    // We define three roles/authorities: ROLE_USER, ROLE_ANALYST, ROLE_MANAGER
    private String authorities;



    /* Return a collection of authorities (roles) granted to the user.
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