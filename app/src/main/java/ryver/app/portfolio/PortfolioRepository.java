package ryver.app.portfolio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    /**
     * Get a Portfolio, filtered by the specified customerId - can return null
     * 
     * @param customerId
     * @return
     */
    Optional<Portfolio> findByCustomerId(Long customerId);

}