package ryver.app.portfolio;

import ryver.app.asset.*;
import ryver.app.customer.*;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.access.prepost.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class PortfolioController {
    private PortfolioRepository portfolios;
    private AssetController assetsCtrl;
    private CustomerRepository customers;

    public PortfolioController(PortfolioRepository portfolios, AssetController assetsCtrl, CustomerRepository customers) {
        this.portfolios = portfolios;
        this.assetsCtrl = assetsCtrl;
        this.customers = customers;
    }

    // @GetMapping("/portfolio")
    // public List<Portfolio> getPortfolios() {
    //     return portfolios.findAll();
    // }

    //@PreAuthorize("authentication.principal.active == true and (hasRole('USER') and #customer_Id == authentication.principal.id)")
    

    @PreAuthorize("authentication.principal.active == true")
    @GetMapping("/portfolio")
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