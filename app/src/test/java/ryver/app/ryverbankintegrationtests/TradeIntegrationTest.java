package ryver.app.ryverbankintegrationtests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

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
import ryver.app.account.AccountRepository;
import ryver.app.account.Account;
import ryver.app.customer.CustomerRepository;
import ryver.app.portfolio.Portfolio;
import ryver.app.portfolio.PortfolioRepository;
import ryver.app.customer.Customer;
import ryver.app.stock.StockRepository;
import ryver.app.stock.CustomStock;
import ryver.app.trade.TradeRepository;
import ryver.app.trade.Trade;


/** Start an actual HTTP server listening at a random port */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public class TradeIntegrationTest {

	@LocalServerPort
	private int port;

	private final String baseUrl = "http://localhost:";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private TradeRepository trades;

	@Autowired
	private CustomerRepository customers;

	@Autowired
	private PortfolioRepository portfolios;

	@Autowired
	private AccountRepository accounts;

	@Autowired
	private StockRepository stocks;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@BeforeAll
	public void init(){

		Customer customer = customers.save(new Customer("user_1", encoder.encode("password"), "ROLE_USER", "User_one", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true));
		Portfolio portfolio = new Portfolio();
		portfolio.setCustomer(customer);
		portfolio.setCustomer_id(customer.getId());
		portfolio.setTotal_gain_loss(0.0);
		portfolio.setUnrealized_gain_loss(0.0);
		portfolios.save(portfolio);
		Account account = accounts.save(new Account(1000000.0, 1000000.0, customer.getId(), customer));
		CustomStock stock = new CustomStock("A17U", 3.1, 20000, 3.07, 20000, 3.08, new ArrayList<Trade>());
		stocks.save(stock);
	}

	@AfterEach
	void tearDown() {
		// clear the database after each test
		trades.deleteAll();
    }

	@Test
	public void createTrade_ValidCustomer_Success() throws Exception{
		String newTrade =
		"{\"action\":\"buy\",\"symbol\":\"A17U\",\"quantity\": 1000,\"bid\": 1.0, \"ask\": 0.0,\"account_id\": 1,\"customer_id\": 1}";
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(newTrade, headers);
		URI uri = new URI(baseUrl + port + "/trades");

		ResponseEntity<Trade> result = restTemplate.withBasicAuth("user_1", "password").postForEntity(uri, entity, Trade.class);
		assertEquals(201, result.getStatusCode().value());
	}

	@Test
	public void createTrade_InvalidCustomer_Failure() throws Exception{
		//Note: JSONObject is just another way to send JSON data other than String
		JSONObject requestParams = new JSONObject();
		requestParams.put("action", "buy");
		requestParams.put("symbol", "A17U"); //find the first stock in the list
        requestParams.put("quantity", 1000); //should be in multiples of 100
		requestParams.put("bid", 1); 
		requestParams.put("ask", 0.0);
		requestParams.put("account_id", 1); //only one account in accounts
		
		requestParams.put("customer_id", 2); //INVALID CUSTOMER_ID
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
		URI uri = new URI(baseUrl + port + "/trades");

		ResponseEntity<Trade> result = restTemplate.withBasicAuth("user_1", "password").postForEntity(uri, entity, Trade.class);
		assertEquals(403, result.getStatusCode().value());
	}

    @Test
    public void getSpecificTrade_ValidTradeId_Success() throws Exception{
		//retrieve a particular trade that a customer has made
		Trade trade = new Trade("buy", "A17U", 1000, 1.0, 0.0, 1L, 1L);
		trade.setAccount(accounts.findAll().get(0));
		trade.setStock(stocks.findAll().get(0));
		trades.save(trade);
			
		URI uri = new URI(baseUrl + port + "/trades/" + trade.getId());

		ResponseEntity<Trade> result = restTemplate.withBasicAuth("user_1", "password").getForEntity(uri, Trade.class);
		
		//requested trade found
		assertEquals(200, result.getStatusCode().value());
	} 

    @Test
    public void getSpecificTrade_InvalidTradeId_Failure() throws Exception{
		//retrieve a particular trade that a customer has made
		//but no trade has been made yet			
		
		URI uri = new URI(baseUrl + port + "/trades/" + 1);

		ResponseEntity<Trade> result = restTemplate.withBasicAuth("user_1", "password").getForEntity(uri, Trade.class);
		
		//requested trade not found
		assertEquals(404, result.getStatusCode().value());
		
	}

	@Test
	public void updateSpecificTrade_ValidCustomer_Success() throws Exception{
		//customer has an existing trade
		Trade trade = new Trade("buy", "A17U", 1000, 1.0, 0.0, 1L, 1L);
		trade.setAccount(accounts.findAll().get(0));
		trade.setStock(stocks.findAll().get(0));
		trade.setStatus("open");
		trades.save(trade);

		//custmer cancels trade 
		JSONObject requestParams = new JSONObject();
		requestParams.put("action", "buy");
		requestParams.put("symbol", "A17U"); //find the first stock in the list
        requestParams.put("quantity", 1000); //should be in multiples of 100
		requestParams.put("bid", 1); 
		requestParams.put("ask", 0.0);
		requestParams.put("status", "cancelled"); // UPDATED STATUS OF TRADE
		requestParams.put("account_id", 1); //only one account in accounts
		
		requestParams.put("customer_id", 1); //only one user in users

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
		URI uri = new URI(baseUrl + port + "/trades/" + trade.getId());

		ResponseEntity<Trade> result = restTemplate.withBasicAuth("user_1", "password").exchange(uri, HttpMethod.PUT, entity, Trade.class);

		//requested trade updated
		assertEquals(200, result.getStatusCode().value());
		assertEquals("cancelled", result.getBody().getStatus());
	}

	@Test
	public void updateSpecificTrade_UnAuthenticatedCustomer_Failure() throws Exception{
		//customer has an existing trade
		Trade trade = new Trade("buy", "A17U", 1000, 1.0, 0.0, 1L, 1L);
		trade.setAccount(accounts.findAll().get(0));
		trade.setStock(stocks.findAll().get(0));
		trade.setStatus("open");
		trades.save(trade);
		
		//custmer cancels trade 
		JSONObject requestParams = new JSONObject();
		requestParams.put("action", "buy");
		requestParams.put("symbol", "A17U"); //find the first stock in the list
        requestParams.put("quantity", 1000); //should be in multiples of 100
		requestParams.put("bid", 1); 
		requestParams.put("ask", 0.0);
		requestParams.put("status", "cancelled"); // UPDATED STATUS OF TRADE
		requestParams.put("account_id", 1); //only one account in accounts
		
		requestParams.put("customer_id", 1); //only one user in users

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
		URI uri = new URI(baseUrl + port + "/trades/" + trade.getId());

		ResponseEntity<Trade> result = restTemplate.withBasicAuth("user_2", "wrongpassword").exchange(uri, HttpMethod.PUT, entity, Trade.class);

		//requested trade updated
		assertEquals(401, result.getStatusCode().value());
	}
	
}