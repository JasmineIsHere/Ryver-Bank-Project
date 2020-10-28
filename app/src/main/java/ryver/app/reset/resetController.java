package ryver.app.reset;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import ryver.app.account.AccountRepository;
import ryver.app.asset.AssetRepository;
import ryver.app.content.ContentRepository;
import ryver.app.customer.CustomerRepository;
import ryver.app.portfolio.PortfolioRepository;
import ryver.app.stock.StockRepository;
import ryver.app.trade.TradeRepository;
import ryver.app.transaction.TransactionRepository;

@RestController
public class resetController {

    private CustomerRepository customers;
    private AccountRepository accounts;
    private ContentRepository contents;
    private TransactionRepository transactions;
    private TradeRepository trades;
    private PortfolioRepository portfolios;
    private AssetRepository assets;
    private StockRepository stocks;

    @GetMapping("/reset")
    public void reset() throws Exception{
        
        customers.deleteAll();
        accounts.deleteAll();
        contents.deleteAll();
        transactions.deleteAll();
        trades.deleteAll();
        portfolios.deleteAll();
        assets.deleteAll();
        stocks.deleteAll();

    }

}
