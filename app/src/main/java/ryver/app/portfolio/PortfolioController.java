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
    private AssetRepository assets;

    public PortfolioController(PortfolioRepository portfolios, AssetRepository assets) {
        this.portfolios = portfolios;
        this.assets = assets;
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
        List<Asset> assetList = assets.findByPortfolioId(portfolio.getId());

        for (Asset asset : assetList) {
            unrealized_gain_loss += asset.getGain_loss().doubleValue();
        }

        // Portfolio takes in double, asset gain_loss is BigDecimal
        portfolio.setUnrealized_gain_loss(unrealized_gain_loss);
        return portfolios.save(portfolio);
    }

    public Portfolio updatePortfolio(long portfolioId, Portfolio updatedPortfolio) {
        if(!portfolios.existsById(portfolioId)) {
            throw new PortfolioNotFoundException(portfolioId);
        }
        return portfolios.findById(portfolioId).map(portfolio -> {
            portfolio.setTotal_gain_loss(updatedPortfolio.getTotal_gain_loss());
            portfolio.setUnrealized_gain_loss(updatedPortfolio.getUnrealized_gain_loss());
            return portfolios.save(portfolio);
        }).orElseThrow(() -> new ryver.app.asset.PortfolioNotFoundException(portfolioId));
    }
    
}