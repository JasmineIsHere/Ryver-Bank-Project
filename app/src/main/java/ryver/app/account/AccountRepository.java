package ryver.app.account;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>{
    // additional derived queries specified here will be implemented by Spring Data JPA
    // start the derived query with "findBy", then reference the entity attributes you want to filter
    List<Account> findByCustomerId(Long customerId);
    Optional<Account> findByIdAndCustomerId(Long accountId, Long customerId);
    Optional<Account> findById(Long accountId);
}