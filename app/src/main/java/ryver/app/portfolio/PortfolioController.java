// package ryver.app.portfolio;

// import java.util.List;

// import ryver.app.customer.Customer;
// import ryver.app.customer.CustomerRepository;
// import ryver.app.customer.CustomerNotFoundException;

// import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.*;

// public class PortfolioController {
//     private PortfolioRepository portfolios;
//     private TradeRepository trades;

//     public PortfolioController(PortfolioRepository portfolios) {
//         this.portfolios = portfolios;
//     }

//     @GetMapping("/portfolio/{customerId}")
//     @PreAuthorize("authentication.principal.active == true and (hasRole('USER') and #customerId == authentication.principal.id)")
//     public Portfolio getPortfolio(@PathVariable (value = "customerId") Long customerId) {
//         Portfolio portfolio = portfolios.findByCustomerId()
//             .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
//         List<Trade> assets = trades.findByCustomerId()
//             .orElseThrow(() -> new CustomerNotFoundException(customerId));

//             portfolio.setAssets(assets);
//             // portfolio.setUnrealized_gain_loss(unrealized_gain_loss);
//             // portfolio.setTotal_gain_loss(total_gain_loss);
//         return portfolio;
//     }
// }