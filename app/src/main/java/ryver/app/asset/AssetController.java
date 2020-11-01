package ryver.app.asset;

import ryver.app.portfolio.*;
import ryver.app.stock.*;
import ryver.app.trade.*;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class AssetController {
    // Repositories
    private AssetRepository assets;
    private PortfolioRepository portfolios;
    private StockRepository stocks;

    public AssetController(AssetRepository assets, PortfolioRepository portfolios, StockRepository stocks) {
        this.assets = assets;
        this.portfolios = portfolios;
        this.stocks = stocks;
    }

    /** 
     * Get a List of Assets associated with the specified portfolioId
     * 
     * @param portfolioId
     * @return List<Asset>
     */
    public List<Asset> getAssetsByPortfolioId(Long portfolioId) {
        if (!portfolios.existsById(portfolioId))
            throw new PortfolioNotFoundException(portfolioId);

        List<Asset> assetList = assets.findByPortfolioId(portfolioId);

        for (Asset asset : assetList) {
            Optional<CustomStock> stock = stocks.findBySymbol(asset.getCode());
            stock.ifPresent(s -> {
                asset.setCurrent_price(s.getBid());
                asset.setValue(asset.getCurrent_price() * asset.getQuantity());
            });
        }
        return assetList;
    }

    
    /** 
     * Create a new Asset with the specified parameters
     * For use in AppApplication
     * 
     * @param stock
     * @param trade
     * @param portfolio
     */
    public void createAssetForAppApplication(CustomStock stock, Trade trade, Portfolio portfolio) {

        long portfolioId = portfolio.getId();
        String code = trade.getSymbol();
        Optional<Asset> nothing = Optional.empty();

        // Asset already exists in portfolio -> update asset
        if (assets.findByCodeAndPortfolioId(code, portfolioId) != nothing) {
            Asset asset = assets.findByCodeAndPortfolioId(code, portfolioId)
                    .orElseThrow(() -> new AssetCodeNotFoundException(code));

            long assetId = asset.getId();
            int prevQuantity = asset.getQuantity();
            double prevAvg_price = asset.getAvg_price();
            double prevTotalPrice = prevQuantity * prevAvg_price;

            int newQuantity = prevQuantity + trade.getQuantity();
            double newTotalPrice = prevTotalPrice + (trade.getQuantity() * stock.getAsk());
            double newAvg_price = newTotalPrice / newQuantity;

            Asset newAsset = asset;
            newAsset.setQuantity(newQuantity);
            newAsset.setAvg_price(newAvg_price);
            updateAsset(portfolioId, assetId, newAsset);

        } else {
            // Asset does not exist in portfolio -> add trade to asset
            int quantity = trade.getQuantity();
            double avg_price = stock.getAsk();
            double current_price = stock.getAsk();
            double value = current_price * quantity;
            double gain_loss = value - (avg_price * quantity);

            Asset asset = new Asset(code, quantity, avg_price, current_price, value, gain_loss);
            asset.setPortfolio(portfolio);
            addAsset(portfolioId, asset);
        }
    }

    /** 
     * Create a new Asset
     * If specified Portfolio ID does not exist - Returns 404 Error
     * 
     * @param portfolioId
     * @param asset
     * @return Asset
     */
    public Asset addAsset(Long portfolioId, Asset asset) {
        return portfolios.findById(portfolioId).map(portfolio -> {
            asset.setPortfolio(portfolio);
            return assets.save(asset);
        }).orElseThrow(() -> new PortfolioNotFoundException(portfolioId));
    }
    
    /** 
     * Updates an Asset with the specified portfolioId and assetId
     * Based on the JSON data
     * 
     * @param portfolioId
     * @param assetId
     * @param newAsset
     * @return Asset
     */
    public Asset updateAsset(Long portfolioId, Long assetId, Asset newAsset) {
        if (!portfolios.existsById(portfolioId)) {
            throw new PortfolioNotFoundException(portfolioId);
        }
        return assets.findByIdAndPortfolioId(assetId, portfolioId).map(asset -> {
            asset.setQuantity(newAsset.getQuantity());
            asset.setAvg_price(newAsset.getAvg_price());
            asset.setCurrent_price(newAsset.getCurrent_price());
            asset.setValue(asset.getCurrent_price() * asset.getQuantity());
            asset.setGain_loss(newAsset.getGain_loss());
            return assets.save(asset);
        }).orElseThrow(() -> new AssetIdNotFoundException(assetId));
    }

    /** 
     * Delete a specified Asset in the specified Portfolio
     * If specified Asset does not exist - Returns 404 Error
     * 
     * @param portfolioId
     * @param assetId
     * @return ResponseEntity<?>
     */
    public ResponseEntity<?> deleteAsset(Long portfolioId, Long assetId) {
        if (!portfolios.existsById(portfolioId))
            throw new PortfolioNotFoundException(portfolioId);

        return assets.findByIdAndPortfolioId(assetId, portfolioId).map(asset -> {
            assets.delete(asset);
            return ResponseEntity.ok().build();
        }).orElseThrow(() -> new AssetIdNotFoundException(assetId));
    }
}