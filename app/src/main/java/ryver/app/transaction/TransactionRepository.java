package ryver.app.transaction;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;


public interface TransactionRepository extends JpaRepository<Transaction, Long>{
    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findByToOrFrom(Long accountId, Long accountId2);
}