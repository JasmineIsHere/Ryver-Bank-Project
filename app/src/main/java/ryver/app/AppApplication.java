package ryver.app;

import ryver.app.customer.*;
import ryver.app.stock.*;
import ryver.app.account.*;
import ryver.app.asset.*;
import ryver.app.content.ContentRepository;
import ryver.app.trade.*;
import ryver.app.portfolio.*;

import java.util.*;
import java.text.DecimalFormat;
import java.time.*;
import java.sql.Timestamp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class AppApplication {

	private static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {

		ctx = SpringApplication.run(AppApplication.class, args);

		load();
	}

	/**
	 * Load the intial objects needed
	 */
	private static void load() {

		CustomerRepository customers = ctx.getBean(CustomerRepository.class);
		AccountRepository accounts = ctx.getBean(AccountRepository.class);
		StockRepository stocks = ctx.getBean(StockRepository.class);

		PortfolioRepository portfolios = ctx.getBean(PortfolioRepository.class);

		AssetRepository assets = ctx.getBean(AssetRepository.class);
		AssetController assetCtrl = new AssetController(assets, portfolios, stocks);

		PortfolioController portfolioCtrl = new PortfolioController(portfolios, assetCtrl, customers);

		TradeRepository trades = ctx.getBean(TradeRepository.class);
		TradeController tradesCtrl = new TradeController(trades, customers, accounts, stocks, portfolios, portfolioCtrl,
				assets, assetCtrl);

		BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);

		StockController stocksCtrl = new StockController(stocks);

		// Create the initial manager and analyst per requirement
		System.out
				.println("[Add manager1]: " + customers.save(new Customer("manager_1", encoder.encode("01_manager_01"),
						"ROLE_MANAGER", "Manager One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)));
		System.out
				.println("[Add analyst1]: " + customers.save(new Customer("analyst_1", encoder.encode("01_analyst_01"),
						"ROLE_ANALYST", "Analyst One", "S8098765B", "99876543", "456 Clementi Road S987456", true)));
		System.out
				.println("[Add analyst2]: " + customers.save(new Customer("analyst_2", encoder.encode("02_analyst_02"),
						"ROLE_ANALYST", "Analyst Two", "S9752354A", "99876552", "678 Orchard Road S987456", true)));

		// Initialize stocks and store into db as per requirement
		System.out.println("\n[Grabbin stocks]: ");
		List<CustomStock> stockList = stocksCtrl.initiateStocks();
		System.out.println("Found " + stockList.size() + " stocks in the STI list");

		// Create test user, account, portfolio for manipulating the market
		Customer customer = customers.save(new Customer("testuser_01", encoder.encode("01_testuser_01"), "ROLE_USER",
				"Test One", "S8098765B", "99876543", "673 Pasir Ris Road S987673", true));
		System.out.println("[Add test customer]: " + customer);
		Long customerId = customer.getId();

		Account account = new Account(100000.0, 100000.0, customerId);
		account.setCustomer(customer);
		System.out.println("[Add test account for test user]: " + accounts.save(account));
		Long accountId = account.getId();

		Portfolio portfolio = new Portfolio();
		portfolio.setCustomer_id(customerId);
		portfolio.setAssets(new ArrayList<Asset>());
		portfolio.setTotal_gain_loss(0.0);
		portfolio.setUnrealized_gain_loss(0.0);
		portfolio.setCustomer(customer);
		System.out.println("[Add portfolio to test user]: " + portfolios.save(portfolio));
		customer.setPortfolio(portfolio);

		// Manipulate the market
		for (CustomStock stock : stockList) {

			// Initial stocks
			Trade trade1 = new Trade("sell", stock.getSymbol(), (int) stock.getAsk_volume(), 0.0, stock.getAsk(), 0,
					"open", accountId, customerId);
			trade1.setAccount(account);
			trade1.setStock(stock);
			trades.save(trade1);

			assetCtrl.createAssetForAppApplication(stock, trade1, portfolio);
			System.out.println("[Add Inital Stocks]: " + trades.save(trade1));

			Trade trade2 = new Trade("buy", stock.getSymbol(), (int) stock.getBid_volume(), stock.getBid(), 0.0, 0,
					"open", accountId, customerId);
			trade2.setAccount(account);
			trade2.setStock(stock);
			System.out.println("[Add Inital Stocks]: " + trades.save(trade2));

		}
	}

	/**
	 * Reset the API
	 */
	public static void restart() {
		CustomerRepository customers = ctx.getBean(CustomerRepository.class);
		AccountRepository accounts = ctx.getBean(AccountRepository.class);
		StockRepository stocks = ctx.getBean(StockRepository.class);
		ContentRepository contents = ctx.getBean(ContentRepository.class);

		PortfolioRepository portfolios = ctx.getBean(PortfolioRepository.class);

		AssetRepository assets = ctx.getBean(AssetRepository.class);
		TradeRepository trades = ctx.getBean(TradeRepository.class);
		customers.deleteAll();
		accounts.deleteAll();
		contents.deleteAll();
		stocks.deleteAll();
		portfolios.deleteAll();
		assets.deleteAll();
		trades.deleteAll();
		load();

	}

}
