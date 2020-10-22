package ryver.app.portfolio;

import java.util.List;

import ryver.app.trade.Trade;
import ryver.app.trade.TradeRepository;
import ryver.app.trade.TradeNotFoundException;

import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;
import ryver.app.customer.CustomerNotFoundException;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

@RestController
public class PortfolioController {
    private PortfolioRepository portfolios;
    private TradeRepository trades;

    public PortfolioController(PortfolioRepository portfolios, TradeRepository trades) {
        this.portfolios = portfolios;
        this.trades = trades;
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
        
        return portfolio;
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