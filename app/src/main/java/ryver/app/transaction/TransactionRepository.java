package ryver.app.transaction;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * Get a List of Transactions, filtered by the specified accountId
     * 
     * @param accountId
     * @return List<Transaction>
     */
    List<Transaction> findByAccountId(Long accountId);

    /**
     * Get a List of Transactions, filtered by the two specified accountId
     * 
     * @param accountId
     * @param accountId2
     * @return List<Transaction>
     */
    List<Transaction> findByToOrFrom(Long accountId, Long accountId2);
}