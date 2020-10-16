package ryver.app.trade;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>{
    // List<Trade> findByCustomerId(Long customerId);
    Trade findByIdAndCustomerId(Long customerId, Long tradeId);
}