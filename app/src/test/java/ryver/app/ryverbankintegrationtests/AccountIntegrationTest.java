package ryver.app.ryverbankintegrationtests;

import ryver.app.account.AccountRepository;
import ryver.app.account.Account;
import ryver.app.customer.*;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Start an actual HTTP server listening at a random port */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AccountIntegrationTest {

	@LocalServerPort
	private int port;

	private final String baseUrl = "http://localhost:";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
    private AccountRepository accounts;
    
    @Autowired
    private CustomerRepository customers;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@AfterEach
	void tearDown() {
        // clear the database after each test
        customers.deleteAll();
		accounts.deleteAll();
    }

    @Test
    public void addAccount_RoleManager_Success() throws Exception{
        //create a manager and account
        customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); // the manager that will retrieve customer account
		Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh",
                "T0046822Z", "82345678", "address", true)); //target customer
        
		String createAccountJSON = 
		"{\"customer_id\":" + customer.getId() + ",\"balance\":" + 10000.0 + ",\"available_balance\":" + 10000.0 + "}";
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(createAccountJSON, headers);
        URI postUri = new URI(baseUrl + port + "/api/accounts");

        //Authority: Manager
        ResponseEntity<Account> result = restTemplate.withBasicAuth("manager_1", "01_manager_01").postForEntity(postUri, entity, Account.class);

        //return 201 successful creation of account
        assertEquals(201, result.getStatusCode().value());
        assertEquals(customer.getId(), result.getBody().getCustomer_id());
    }

    @Test
    public void addAccount_RoleCustomer_Failure() throws Exception{
        //create a customer and account
		Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh",
                "T0046822Z", "82345678", "address", true)); //target customer
        
		String createAccountJSON = 
		"{\"customer_id\":" + customer.getId() + ",\"balance\":" + 10000.0 + ",\"available_balance\":" + 10000.0 + "}";
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(createAccountJSON, headers);
        URI postUri = new URI(baseUrl + port + "/api/accounts");

        //Authority: User
        ResponseEntity<Account> result = restTemplate.withBasicAuth("User_1", "password").postForEntity(postUri, entity, Account.class);

        //return 403 forbidden as User cannot add account
        assertEquals(403, result.getStatusCode().value());
    }

	@Test
	public void getAccountByAccountIdAndCustomerId_RoleCustomer_Success() throws Exception {
        //create customer and manager
        customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); // the manager that will retrieve customer account
		Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh", "T0046822Z", "82345678", "address", true)); //target customer
        
		String createAccountJSON = 
		"{\"customer_id\":" + customer.getId() + ",\"balance\":" + 10000.0 + ",\"available_balance\":" + 10000.0 + "}";
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(createAccountJSON, headers);
        URI postUri = new URI(baseUrl + port + "/api/accounts");

        //Manager creates user account
        ResponseEntity<Account> acc = restTemplate.withBasicAuth("manager_1", "01_manager_01").postForEntity(postUri, entity, Account.class);

        URI getUri = new URI(baseUrl + port + "/api/accounts/" + acc.getBody().getId());

        //Authority: User
		ResponseEntity<Account> result = restTemplate.withBasicAuth("User_1", "password").getForEntity(getUri, Account.class);

		assertEquals(200, result.getStatusCode().value());
    }
}