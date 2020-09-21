package blah.blah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import blah.blah.client.RestTemplateClient;
import blah.blah.user.User;
import blah.blah.user.UserRepository;
import blah.blah.account.Account;
import blah.blah.account.AccountRepository;

@SpringBootApplication
public class ProjectApplication{

	public static void main(String[] args) {
		
		ApplicationContext ctx = SpringApplication.run(ProjectApplication.class, args);

        UserRepository users = ctx.getBean(UserRepository.class);
        BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);
        System.out.println("[Add manager]: " + users.save(new User("manager_1", encoder.encode("01_manager_01"), "ROLE_ADMIN")).getUsername());
        System.out.println("[Add analyst1]: " + users.save(new User("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST")).getUsername());
        System.out.println("[Add analyst2]: " + users.save(new User("analyst_2", encoder.encode("02_analyst_02"), "ROLE_ANALYST")).getUsername());
        System.out.println("[Add user1]: " + users.save(new User("good_user_1", encoder.encode("01_user_01"), "ROLE_USER")).getUsername());
        System.out.println("[Add user2]: " + users.save(new User("good_user_2", encoder.encode("02_user_02"), "ROLE_USER")).getUsername());
        
        AccountRepository accounts = ctx.getBean(AccountRepository.class);
        System.out.println("[Add Account for user1]: " + accounts.save(new Account(123456,12345,50000.0)).getAccountId());
        System.out.println("[Add Account for user2]: " + accounts.save(new Account(123457,12346,100000.0)).getAccountId());

        RestTemplateClient client = ctx.getBean(RestTemplateClient.class);
        //add client tests
    }
    
}
