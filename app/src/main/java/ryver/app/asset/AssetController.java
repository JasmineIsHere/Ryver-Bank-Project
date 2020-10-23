package ryver.app.asset;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ryver.app.portfolio.*;

import ryver.app.stock.CustomStock;
import ryver.app.stock.StockRepository;

@RestController
public class AssetController {
    private AssetRepository assets;
    private PortfolioRepository portfolios;
    private StockRepository stocks;

    public AssetController(AssetRepository assets, PortfolioRepository portfolios) {
        this.assets = assets;
        this.portfolios = portfolios;
    }

    public List<Asset> getAssetsByPortfolioId(Long portfolioId) {
        if(!portfolios.existsById(portfolioId)) 
            throw new PortfolioNotFoundException(portfolioId);
        
        List<Asset> assetList = assets.findByPortfolioId(portfolioId);

        for(Asset asset : assetList){
            Optional<CustomStock> stock = stocks.findBySymbol(asset.getCode());
            stock.ifPresent(s -> {
                asset.setCurrent_price(s.getBid());
                asset.setValue(asset.getCurrent_price() * asset.getQuantity());
            });
        }
        return assetList;
    }
    
    public Asset addAsset(Long portfolioId, Asset asset) {
        return portfolios.findById(portfolioId).map(portfolio ->{
            asset.setPortfolio(portfolio);
            return assets.save(asset);
        }).orElseThrow(() -> new PortfolioNotFoundException(portfolioId));
    }

    public Asset updateAsset(Long portfolioId, Long assetId, Asset newAsset) {
        if(!portfolios.existsById(portfolioId)) {
            throw new PortfolioNotFoundException(portfolioId);
        }
        return assets.findByIdAndPortfolioId(assetId, portfolioId).map(asset -> {
            asset.setQuantity(newAsset.getQuantity());
            asset.setAvg_price(newAsset.getAvg_price());
            asset.setCurrent_price(newAsset.getCurrent_price());
            asset.setValue(newAsset.getValue());
            asset.setGain_loss(newAsset.getGain_loss());
            return assets.save(asset);
        }).orElseThrow(() -> new AssetIdNotFoundException(assetId));
    }

    public ResponseEntity<?> deleteAsset(Long portfolioId, Long assetId) {
        if(!portfolios.existsById(portfolioId)) 
            throw new PortfolioNotFoundException(portfolioId);
    
        return assets.findByIdAndPortfolioId(assetId, portfolioId).map(asset -> {
            assets.delete(asset);
            return ResponseEntity.ok().build();
        }).orElseThrow(() -> new AssetIdNotFoundException(assetId));
    }
}