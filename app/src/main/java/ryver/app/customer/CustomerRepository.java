package ryver.app.customer;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    /**
     * Get a Customer, filtered by the specified username - can return null
     * 
     * @param username
     * @return Optional<Customer>
     */
    Optional<Customer> findByUsername(String username);

    /**
     * Get a List of Customers, filtered by the specified authorities
     * 
     * @param authorities
     * @return List<Customer>
     */
    List<Customer> findByAuthorities(String authorities);
} 