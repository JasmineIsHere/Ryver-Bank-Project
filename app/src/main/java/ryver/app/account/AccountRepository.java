package ryver.app.account;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Get a List of Accounts, filtered by the specified customerId
     * 
     * @param customerId
     * @return List<Account>
     */
    List<Account> findByCustomerId(Long customerId);

    /**
     * Get an Account, filtered by the specified accountId - can return null
     * 
     * @param accountId
     * @return Optional<Account>
     */
    Optional<Account> findById(Long accountId);

    /**
     * Get an Account, filtered by the specified accountId - can return null
     * 
     * @param accountId
     * @param customerId
     * @return Optional<Account>
     */
    Optional<Account> findByIdAndCustomerId(Long accountId, Long customerId);
}