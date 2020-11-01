package ryver.app.customer;

import java.util.*;

import ryver.app.account.*;
import ryver.app.portfolio.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.*;

// Spring Annotations
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

/*
 * Implementations of UserDetails to provide user information to Spring
 * Security, e.g., what authorities (roles) are granted to the customer and
 * whether the account is enabled or not
 */
public class Customer implements UserDetails {
    // Fields
    private static final long serialVersionUID = 1L;

    /**
     * The auto-generated ID for each Account Starts from 1
     */
    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    /**
     * The username of the Customer Usernames are minimum 5 characters, maximum 20
     * characters and cannot be null
     */
    @NotNull(message = "Username should not be null")
    @Size(min = 5, max = 20, message = "Username should be between 5 and 20 characters")
    private String username;

    /**
     * The password of the Customer Passwords are at least 8 characters and cannot
     * be null
     */
    @NotNull(message = "Password should not be null")
    @Size(min = 8, message = "Password should be at least 8 characters. ")
    private String password;

    /**
     * The authority of the Customer We define three roles/authorities: ROLE_USER,
     * ROLE_MANAGER or ROLE_ANALYST Authorities cannot be null
     */
    @NotNull(message = "Authorities should not be null")
    private String authorities;

    /**
     * The full name of the Customer Full names are minimum 5 characters, maximum 20
     * characters and cannot be null
     */
    @JsonProperty("full_name")
    @NotNull(message = "Full Name should not be null")
    @Size(min = 5, max = 20, message = "Full name should be between 5 and 20 characters. ")
    private String fullName;

    /**
     * The NRIC of the Customer The NRIC must be valid
     */
    @NotNull(message = "NRIC should not be null")
    @Size(min = 9, max = 9, message = "NRIC must be valid. ")
    private String nric;

    /**
     * The phone number of the Customer Phone numbers must be valid and cannot be
     * null
     */
    @NotNull(message = "Phone Number should not be null")
    @Pattern(regexp = "[689][0-9]{7}", message = "Phone number must be valid. ")
    private String phone;

    /**
     * The address of the Customer Addresses are minimum 5 characters, maximum 200
     * characters and cannot be null
     */
    @NotNull(message = "Address should not be null")
    @Size(min = 5, max = 200, message = "Address should be between 5 and 200 characters. ")
    private String address;

    /**
     * The active status of the Customer
     */
    private boolean active;

    // Mappings
    /**
     * The List of Accounts associated with the Customer
     */
    @OneToMany(mappedBy = "customer", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Account> accounts;

    /**
     * The Portfolio associated with the Customer
     */
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonIgnore
    private Portfolio portfolio;

    // Constructors

    /**
     * Create a Customer with the specified parameters
     *
     * @param username
     * @param password
     * @param authorities
     * @param fullName
     * @param nric
     * @param phone
     * @param address
     * @param active
     */
    public Customer(String username, String password, String authorities, String fullName, String nric, String phone,
            String address, boolean active) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;

        this.fullName = fullName;
        this.nric = nric;
        this.phone = phone;
        this.address = address;
        this.active = active;
    }

    /**
     * Return a collection of authorities granted to the user
     * 
     * @return Collection<? extends GrantedAuthority>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority(authorities));
    }

    /**
     * The various is___Expired() methods return a boolean to indicate whether or
     * not the user’s account is enabled or expired.
     */

    /**
     * Return the expired status of the Customer
     * 
     * @return boolean
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Return the locked/unlocked status of the Customer
     * 
     * @return boolean
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Return the expired status of the Customer's credentials
     * 
     * @return boolean
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Return the enabled status of the Customer
     * 
     * @return boolean
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}