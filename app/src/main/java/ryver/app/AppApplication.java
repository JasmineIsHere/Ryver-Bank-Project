package ryver.app;

import java.util.List;
import java.text.DecimalFormat;
import java.time.*;
import java.sql.Timestamp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;

import ryver.app.stock.CustomStock;
import ryver.app.stock.StockController;
import ryver.app.stock.StockRepository;

import ryver.app.account.Account;
import ryver.app.account.AccountRepository;

import ryver.app.trade.Trade;
import ryver.app.trade.TradeController;
import ryver.app.trade.TradeRepository;

//import ryver.app.portfolio.*;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);

        CustomerRepository customers = ctx.getBean(CustomerRepository.class);
		BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);

		System.out.println("[Add manager1]: " + customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)));
		System.out.println("[Add analyst1]: " + customers.save(new Customer("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST", "Analyst One", "S8098765B", "99876543", "456 Clementi Road S987456", true)));
		
		Customer customer = customers.save(new Customer("testuser_01", encoder.encode("01_testuser_01"), "ROLE_USER", "Test One", "S8098765B", "99876543", "673 Pasir Ris Road S987673", true));
		System.out.println("[Add test customer]: " + customer);
		
		AccountRepository accounts = ctx.getBean(AccountRepository.class);
		Account account = new Account(100000.0, 100000.0, 3);
		account.setCustomer(customer);
		System.out.println("[Add test account]: " + accounts.save(account));

		StockRepository stocks = ctx.getBean(StockRepository.class);

		//PortfolioRepository portfolios = ctx.getBean(PortfolioRepository.class);

		TradeRepository trades = ctx.getBean(TradeRepository.class);
		// TradeController tradesCtrl = new TradeController(trades, customers, accounts, stocks, portfolios);
		
		// StockController stocksCtrl = new StockController(stocks, tradesCtrl);

		TradeController tradesCtrl = new TradeController(trades, customers, accounts, stocks);
		StockController stocksCtrl = new StockController(stocks);

		System.out.println("\n[Grabbin stocks]: ");
		List<CustomStock> stockList = stocksCtrl.initiateStocks();
		System.out.println("Found " + stockList.size() + " stocks in the STI list");

		for (CustomStock stock: stockList){
			System.out.println("[Add in stock]: " + stocks.save(stock));
			String symbol = stock.getSymbol();
			System.out.println(symbol);
			
			// initial stocks
			Trade trade1 = new Trade("sell", symbol, (int)stock.getAsk_volume(), 0.0, stock.getAsk().doubleValue(), 0, "open", 1L, 3L);
			trade1.setAccount(account);
			trade1.setStock(stock);
			trades.save(trade1);

			Trade trade2 = new Trade("buy", symbol, (int)stock.getBid_volume(), stock.getBid().doubleValue(), 0.0, 0, "open", 1L, 3L);
			trade2.setAccount(account);
			trade2.setStock(stock);
			trades.save(trade2);

			

			DecimalFormat df = new DecimalFormat("#.#");
			// random -> 0 to 1
			double rand1 = Math.random();
			double formattedRand1 = Double.parseDouble(df.format(rand1));
			int quantity1 = (int)(formattedRand1 * 10000);

			double stockBid = stock.getBid().doubleValue();
			// get random bid price -> (Math.random() * (max - min)) + min
			double randBid = (Math.random() * ((stockBid - 0.1) - (stockBid - 0.3))) + (stockBid - 0.3);
			double formattedRandBid = Double.parseDouble(df.format(randBid));
			
			
			// limit buy order
			if (quantity1 != 0) {
				// milliseconds
				long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
				// datetime
				LocalDateTime datetime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
				System.out.println(datetime);
				
				Trade trade = new Trade("buy", symbol, quantity1, formattedRandBid, 0.0, timestamp, "open", 1L, 3L);
				trade.setAccount(account);
				trade.setStock(stock);
				System.out.println("[Add market maker's trades]: " + trades.save(trade));
				
				// if this trade's bid is lower than the stock's previous ask
				// if this trade's bid is higher than the stock's previous bid
				// -> save new bid price and quantity into the stocks database
				tradesCtrl.updateTradeToStock(trade, stock);
			}

			double rand2 = Math.random();
			double formattedRand2 = Double.parseDouble(df.format(rand2));
			int quantity2 = (int)(formattedRand2 * 10000);

			double stockAsk = stock.getAsk().doubleValue();
			// get random ask price -> (Math.random() * (max - min)) + min
			double randAsk = (Math.random() * ((stockAsk + 0.3) - (stockAsk + 0.1))) + (stockAsk + 0.1);
			double formattedRandAsk = Double.parseDouble(df.format(randAsk));
			
			// limit sell order
			if (quantity2 != 0) {
				// milliseconds
				long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
				// datetime
				LocalDateTime datetime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
				System.out.println(datetime);
				
				// Trade trade = new Trade("sell", symbol, quantity2, 0.0, 3.2, timestamp, "open", 1L, 3L);
				Trade trade = new Trade("sell", symbol, quantity2, 0.0, formattedRandAsk, timestamp, "open", 1L, 3L);
				trade.setAccount(account);
				trade.setStock(stock);
				System.out.println("[Add market maker's trades]: " + trades.save(trade));
				
				// if this trade's ask is higher than the stock's previous bid
				// if this trade's ask is lower than the stock's previous ask 
				// -> save new ask price and quantity into the stocks database
				tradesCtrl.updateTradeToStock(trade, stock);
			}

		}

		// RestTemplateClient client = ctx.getBean(RestTemplateClient.class);
	}

}
