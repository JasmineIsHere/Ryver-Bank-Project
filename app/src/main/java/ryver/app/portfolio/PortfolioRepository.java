package ryver.app.portfolio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // define a derived query to find user by username
    Optional<Portfolio> findByCustomerId(Long customer_id);
    
}