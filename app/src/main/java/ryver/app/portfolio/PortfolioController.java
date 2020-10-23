package ryver.app.portfolio;

import ryver.app.asset.*;
import ryver.app.customer.CustomerNotFoundException;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

@RestController
public class PortfolioController {
    private PortfolioRepository portfolios;
    private AssetController assetsCtrl;

    public PortfolioController(PortfolioRepository portfolios) {
        this.portfolios = portfolios;
    }

    @GetMapping("/portfolio")
    public List<Portfolio> getPortfolios() {
        return portfolios.findAll();
    }

    @GetMapping("/portfolio/{customerId}")
    @PreAuthorize("authentication.principal.active == true and (hasRole('USER') and #customerId == authentication.principal.id)")
    public Portfolio getPortfolio(@PathVariable (value = "customerId") Long customerId) {
        Portfolio portfolio = portfolios.findByCustomerId(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        // Calculate unrealized gain/loss
        double unrealized_gain_loss = 0.0;
        List<Asset> assetList = assetsCtrl.getAssetsByPortfolioId(portfolio.getId());

        for (Asset asset : assetList) {
            asset.setGain_loss((asset.getCurrent_price() * asset.getQuantity()) - (asset.getAvg_price() * asset.getQuantity()));
            unrealized_gain_loss += asset.getGain_loss();
        }

        portfolio.setUnrealized_gain_loss(unrealized_gain_loss);
        return portfolios.save(portfolio);
    }

    public Portfolio updatePortfolio(long portfolioId, Portfolio updatedPortfolio) {
        if(!portfolios.existsById(portfolioId)) {
            throw new PortfolioNotFoundException(portfolioId);
        }
        return portfolios.findById(portfolioId).map(portfolio -> {
            portfolio.setTotal_gain_loss(updatedPortfolio.getTotal_gain_loss());

            double unrealized_gain_loss = 0.0;

            for (Asset asset : updatedPortfolio.getAssets()) {
                asset.setGain_loss((asset.getCurrent_price() * asset.getQuantity()) - (asset.getAvg_price() * asset.getQuantity()));
                unrealized_gain_loss += asset.getGain_loss();
            }

            portfolio.setUnrealized_gain_loss(unrealized_gain_loss);
            return portfolios.save(portfolio);
        }).orElseThrow(() -> new ryver.app.asset.PortfolioNotFoundException(portfolioId));
    }
    
}