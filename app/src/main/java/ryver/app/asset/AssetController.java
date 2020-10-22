package ryver.app.asset;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;
import ryver.app.customer.CustomerNotFoundException;

import ryver.app.portfolio.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.*;
import org.springframework.http.HttpStatus;


@RestController
public class AssetController {
    private AssetRepository assets;
    private PortfolioRepository portfolios;

    public AssetController(AssetRepository assets, PortfolioRepository portfolios) {
        this.assets = assets;
        this.portfolios = portfolios;
    }

    public List<Asset> getAssetsByPortfolioId(Long portfolioId) {
        if(!portfolios.existsById(portfolioId)) 
            throw new PortfolioNotFoundException(portfolioId);
        
        return assets.findByPortfolioId(portfolioId);
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
        }).orElseThrow(() -> new AssetNotFoundException(assetId));
    }

    public ResponseEntity<?> deleteAsset(Long portfolioId, Long assetId) {
        if(!portfolios.existsById(portfolioId)) 
            throw new PortfolioNotFoundException(portfolioId);
    

        return assets.findByIdAndPortfolioId(assetId, portfolioId).map(asset -> {
            assets.delete(asset);
            return ResponseEntity.ok().build();
        }).orElseThrow(() -> new AssetNotFoundException(assetId));
    }
}