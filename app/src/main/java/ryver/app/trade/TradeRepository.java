package ryver.app.trade;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>{
    // CREATED FOR TESTING. NOT NEEDED FOR SUBMISSION
    List<Trade> findByCustomerId(Long customerId);
    List<Trade> findByStatusAndSymbol(String status, String symbol);
    
    Optional<Trade> findByIdAndCustomerId(Long customerId, Long tradeId);
}