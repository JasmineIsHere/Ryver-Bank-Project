package ryver.app.stock;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<CustomStock, String>{
    Optional<CustomStock> findBySymbol(String symbol);
}