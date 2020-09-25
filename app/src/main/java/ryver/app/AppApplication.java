package ryver.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ryver.app.user.User;
import ryver.app.user.UserRepository;
import ryver.app.account.Account;
import ryver.app.account.AccountRepository;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(AppApplication.class, args);

        UserRepository users = ctx.getBean(UserRepository.class);
		BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);
		System.out.println("[Add manager]: " + users.save(new User("manager_1", encoder.encode("01_manager_01"), "ROLE_ADMIN")));
		System.out.println("[Add analyst1]: " + users.save(new User("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST")));

		System.out.println("[Add user1]: new userID = " + users.save(new User("good_user_1", encoder.encode("01_user_01"), "ROLE_USER")));

		
		// AccountRepository accounts = ctx.getBean(AccountRepository.class);
		
		// System.out.println("[Add Account for user1]: new AccountID = " + accounts.save(user1.getUID(),12345L,50000,5000));

    	// RestTemplateClient client = ctx.getBean(RestTemplateClient.class);
	}

}
