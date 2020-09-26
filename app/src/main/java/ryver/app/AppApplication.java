package ryver.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;
import ryver.app.account.Account;
import ryver.app.account.AccountRepository;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);

        CustomerRepository customers = ctx.getBean(CustomerRepository.class);
		BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);
		System.out.println("[Add manager1]: " + customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_ADMIN")));
		System.out.println("[Add analyst1]: " + customers.save(new Customer("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST")));

		// customer should be added in restClient
		// System.out.println("[Add customer1]: " + customers.save(new Customer("good_user_1", encoder.encode("01_user_01"), "ROLE_USER")));

		
		// AccountRepository accounts = ctx.getBean(AccountRepository.class);
		// System.out.println("[Add Account for customer1]: new AccountID = " + accounts.save(customer1.getUID(),12345L,50000,5000));

    	// RestTemplateClient client = ctx.getBean(RestTemplateClient.class);
	}

}
