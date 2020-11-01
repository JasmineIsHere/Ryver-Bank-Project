package ryver.app.trade;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    /**
     * Get a List of Trades, filtered by the specified customerId
     * Created for testing only
     * 
     * @param customerId
     * @return List<Trade>
     */
    List<Trade> findByCustomerId(Long customerId);
    
    /**
     * Get a List of Trades, filtered by the specified action, status and symbol
     * 
     * @param action
     * @param status
     * @param symbol
     * @return List<Trade>
     */
    List<Trade> findByActionAndStatusAndSymbol(String action, String status, String symbol);

    /**
     * Get a Trade, filtered by the specified tradeId and customerId - can return null
     * 
     * @param tradeId
     * @param customerId
     * @return Optional<Trade>
     */
    Optional<Trade> findByIdAndCustomerId(Long tradeId, Long customerId);
}