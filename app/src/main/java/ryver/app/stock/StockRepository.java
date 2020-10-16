package ryver.app.stock;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<CustomStock, String>{
    Optional<CustomStock> findBySymbol(String symbol);
}