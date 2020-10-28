package ryver.app.ryverbankintegrationtests;

import ryver.app.stock.StockRepository;
import ryver.app.trade.Trade;
import ryver.app.customer.Customer;
import ryver.app.customer.CustomerRepository;
import ryver.app.stock.CustomStock;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.ArrayList;

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
class StockIntegrationTest {

	@LocalServerPort
	private int port;

	private final String baseUrl = "http://localhost:";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
    private StockRepository stocks;

    @Autowired
    private CustomerRepository customers;

    @Autowired
    private BCryptPasswordEncoder encoder;

	@AfterEach
	void tearDown() {
        // clear the database after each test
		stocks.deleteAll();
    }
    
    // @Test
    // public void getStockBySymbol_ValidSymbol_Success() throws Exception{
    //     Customer customer = new Customer("user_1", encoder.encode("password"), "ROLE_USER", "user_fullname", "S7812345A", "91234567", "address", true);
    //     customer.setId(1L);
    //     customers.save(customer);
        
    //     CustomStock stock = new CustomStock(
    //         "V03", 20.55, 20000, 20.60, 20000, 20.65, new ArrayList<Trade>());
    //     stocks.save(stock);

    //     URI postUri = new URI(baseUrl + port + "/stocks/" + stock.getSymbol());

    //     ResponseEntity<CustomStock> result = restTemplate.withBasicAuth("user_1", "password").getForEntity(postUri, CustomStock.class);

    //     assertEquals(200, result.getStatusCode().value());
    // }
    
    // @Test
    // public void getStockBySymbol_InvalidSymbol_Failure() throws Exception{
    //     Customer customer = new Customer("user_1", encoder.encode("password"), "ROLE_USER", "user_fullname", "S7812345A", "91234567", "address", true);
    //     customer.setId(1L);
    //     customers.save(customer);

    //     CustomStock stock = new CustomStock(
    //         "V03", 20.55, 20000, 20.60, 20000, 20.65, new ArrayList<Trade>());
    //     stocks.save(stock);

    //     URI uri = new URI(baseUrl + port + "/stocks/V01");

	// 	ResponseEntity<CustomStock> result = restTemplate.withBasicAuth("user_1", "password").getForEntity(uri, CustomStock.class);
		
	// 	assertEquals(200, result.getStatusCode().value());
    // }
}