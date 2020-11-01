package ryver.app.asset;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    /**
     * Get a List of Assets, filtered by the specified portfolioId
     * 
     * @param portfolioId
     * @return
     */
    List<Asset> findByPortfolioId(Long portfolioId);

    /**
     * Get an Asset, filtered by the specified assetId and portfolioId - can return null
     * 
     * @param assetId
     * @param portfolioId
     * @return
     */
    Optional<Asset> findByIdAndPortfolioId(Long assetId, Long portfolioId);

    /**
     * Get an Asset, filtered by the specified Code and portfolioId - can return null
     * 
     * @param Code
     * @param portfolioId
     * @return
     */
    Optional<Asset> findByCodeAndPortfolioId(String Code, Long portfolioId);
}