package ryver.app.ryverbankintegrationtests;

import ryver.app.account.AccountRepository;
import ryver.app.account.Account;
import ryver.app.transaction.TransactionRepository;
import ryver.app.transaction.Transaction;
import ryver.app.customer.*;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
        URI postUri = new URI(baseUrl + port + "/api/accounts/"+ account1.getId() +"/transactions");

        //Authority: user
        ResponseEntity<Transaction> result = restTemplate.withBasicAuth("user_1", "01_user_01").postForEntity(postUri, entity, Transaction.class);

        //201 succesful addition of transaction
        assertEquals(201, result.getStatusCode().value());
    }
}

