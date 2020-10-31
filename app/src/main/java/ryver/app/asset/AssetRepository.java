package ryver.app.asset;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByPortfolioId(Long portfolioId);
    Optional<Asset> findByIdAndPortfolioId(Long assetId, Long portfolioId);

    Optional<Asset> findByCodeAndPortfolioId(String Code, Long portfolioId);
}