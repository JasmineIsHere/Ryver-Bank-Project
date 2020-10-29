package ryver.app.ryverbankintegrationtests;

import ryver.app.account.AccountRepository;
import ryver.app.account.Account;
import ryver.app.transaction.TransactionRepository;
import ryver.app.transaction.Transaction;
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
import net.minidev.json.JSONObject;

/** Start an actual HTTP server listening at a random port */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TransactionIntegrationTest {

	@LocalServerPort
	private int port;

	private final String baseUrl = "http://localhost:";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
    private AccountRepository accounts;
    
    @Autowired
    private TransactionRepository transactions;
    
    @Autowired
    private CustomerRepository customers;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@AfterEach
	void tearDown() {
        // clear the database after each test
        customers.deleteAll();
        accounts.deleteAll();
        transactions.deleteAll();
    }
    
    @Test
    public void addTransaction_ValidUsers_Success() throws Exception{
        //create customers and accounts
        Customer customer1 = customers.save(new Customer("user_1", encoder.encode("01_user_01"), "ROLE_USER", "Jerry Loh","T0046822Z", "82345678", "address", true)); 
        Customer customer2 = customers.save(new Customer("user_2", encoder.encode("02_user_02"), "ROLE_USER", "User two","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

        Account account1 = accounts.save(new Account(10000.0, 10000.0, 1L, customer1));
        Account account2 = accounts.save(new Account(10000.0, 10000.0, 2L, customer2));

        //create transaction
        JSONObject requestParams = new JSONObject();
        requestParams.put("from", account1.getId()); 
        requestParams.put("to", account2.getId());
        requestParams.put("amount", 100.0);
        requestParams.put("account",  account1);

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
        URI postUri = new URI(baseUrl + port + "/accounts/"+ account1.getId() +"/transactions");

        //Authority: user
        ResponseEntity<Transaction> result = restTemplate.withBasicAuth("user_1", "01_user_01").postForEntity(postUri, entity, Transaction.class);

        //200 succesful addition of transaction
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    public void addTransaction_InvalidUser_Failure() throws Exception{
        Customer customer1 = customers.save(new Customer("user_1", encoder.encode("01_user_01"), "ROLE_USER", "Jerry Loh","T0046822Z", "82345678", "address", true)); 
        Account account1 = accounts.save(new Account(10000.0, 10000.0, 1L, customer1));

        JSONObject requestParams = new JSONObject();
        requestParams.put("from", account1.getId()); 
        requestParams.put("to", 4L); //4L is a fake id
        requestParams.put("amount", 100.0);
        requestParams.put("account",  account1);

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
        URI postUri = new URI(baseUrl + port + "/accounts/"+ account1.getId() +"/transactions");

        ResponseEntity<Transaction> result = restTemplate.withBasicAuth("user_1", "01_user_01").postForEntity(postUri, entity, Transaction.class);

        assertEquals(404, result.getStatusCode().value());
    }
}

    // @Test
    // public void getAllTransactionsByAccountId_ValidId_Success() throws Exception{
    //     Customer customer1 = new Customer("user_1", encoder.encode("password"), "ROLE_USER", "user_fullname", "S7812345A", "91234567", "address", true);
    //     customer1.setId(1L);
    //     customers.save(customer1);

    //     // Customer customer1 = customers.save(new Customer("user_1", encoder.encode("01_user_01"), "ROLE_USER", "Jerry Loh","T0046822Z", "82345678", "address", true)); 
    //     Customer customer2 = customers.save(new Customer("user_2", encoder.encode("02_user_02"), "ROLE_USER", "User two","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

    //     Account account1 = accounts.save(new Account(10000.0, 10000.0, 1L, customer1));
    //     Account account2 = accounts.save(new Account(10000.0, 10000.0, 2L, customer2));

    //     Transaction transaction = transactions.save(new Transaction(account1.getId(), account2.getId(), 100.0, account1));

    //     URI postUri = new URI(baseUrl + port + "/accounts/"+ account1.getId() + "/transactions");

    //     ResponseEntity<Transaction> result = restTemplate.withBasicAuth("user_1", "password").getForEntity(postUri, Transaction.class);
    //     // ResponseEntity<Transaction> result = restTemplate.withBasicAuth("user_1", "01_user_01").getForEntity(postUri, Transaction.class);

    //     assertEquals(200, result.getStatusCode().value());
    // }

    // @Test
    // public void getAllTransactionsByAccountId_InvalidId_Failure() throws Exception{
    //     Customer customer1 = new Customer("user_1", encoder.encode("password"), "ROLE_USER", "user_fullname", "S7812345A", "91234567", "address", true);
    //     customer1.setId(1L);
    //     customers.save(customer1);

    //     // Customer customer1 = customers.save(new Customer("user_1", encoder.encode("01_user_01"), "ROLE_USER", "Jerry Loh","T0046822Z", "82345678", "address", true)); 
    //     Customer customer2 = customers.save(new Customer("user_2", encoder.encode("02_user_02"), "ROLE_USER", "User two","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

    //     Account account1 = accounts.save(new Account(10000.0, 10000.0, 1L, customer1));
    //     Account account2 = accounts.save(new Account(10000.0, 10000.0, 2L, customer2));

    //     Transaction transaction = transactions.save(new Transaction(account1.getId(), account2.getId(), 100.0, account1));

    //     URI postUri = new URI(baseUrl + port + "/accounts/"+ account1.getId() + "/transactions");

    //     ResponseEntity<Transaction> result = restTemplate.withBasicAuth("user_1", "password").getForEntity(postUri, Transaction.class);
    //     // ResponseEntity<Transaction> result = restTemplate.withBasicAuth("user_1", "01_user_01").getForEntity(postUri, Transaction.class);

    //     assertEquals(200, result.getStatusCode().value());
    // }

