package ryver.app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;
import ryver.app.stock.StockController;
import ryver.app.stock.StockRepository;
import ryver.app.stock.CustomStock;
import ryver.app.account.Account;
import ryver.app.account.AccountRepository;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);

        CustomerRepository customers = ctx.getBean(CustomerRepository.class);
		StockRepository stocks = ctx.getBean(StockRepository.class);
		StockController stocksCtrl = new StockController(stocks);
		BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);
		System.out.println("[Add manager1]: " + customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)));
		System.out.println("[Add analyst1]: " + customers.save(new Customer("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST", "Analyst One", "S8098765B", "99876543", "456 Clementi Road S987456", true)));
		
		System.out.println("\n[Grabbin stocks]: ");
		List<CustomStock> stockList = stocksCtrl.initiateStocks();
		System.out.println("Found " + stockList.size() + " stocks in the STI list");
		for (CustomStock stock: stockList){
			System.out.println("[Addin stock]: " + stocks.save(stock));
		}

		// RestTemplateClient client = ctx.getBean(RestTemplateClient.class);
	}

}
