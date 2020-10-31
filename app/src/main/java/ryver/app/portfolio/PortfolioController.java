package ryver.app.portfolio;

import ryver.app.asset.*;
import ryver.app.customer.*;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class PortfolioController {
    private PortfolioRepository portfolios;
    private AssetController assetsCtrl;
    private CustomerRepository customers;

    public PortfolioController(PortfolioRepository portfolios, AssetController assetsCtrl, CustomerRepository customers) {
        this.portfolios = portfolios;
        this.assetsCtrl = assetsCtrl;
        this.customers = customers;
    }

    @GetMapping("/api/portfolio")
    public Portfolio getPortfolio() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = authentication.getName();
        
        Customer customer = customers.findByUsername(customerUsername)
            .orElseThrow(() -> new CustomerNotFoundException(customerUsername));
        
        long customerId = customer.getId();

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