package ryver.app.transaction;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
    // additional derived queries specified here will be implemented by Spring Data JPA
    // start the derived query with "findBy", then reference the entity attributes you want to filter

    // List<Transaction> findByAccountIdAndCustomerId(Long accountId, Long customerId);

    List<Transaction> findByAccountId(Long accountId);
}