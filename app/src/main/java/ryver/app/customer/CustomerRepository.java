package ryver.app.customer;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // define a derived query to find user by username
    Optional<Customer> findByUsername(String username);

    List<Customer> findByAuthorities(String authorities);
} 