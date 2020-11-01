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

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


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
        customers.deleteAll();
    }
    
    @Test
    public void getStockBySymbol_ValidUser_Success() throws Exception{
        //creat customer and stock
        Customer customer = new Customer("user_1", encoder.encode("password"), "ROLE_USER", "user_fullname", "S7812345A", "91234567", "address", true);
        customer.setId(1L);
        customers.save(customer);
        
        CustomStock stock = new CustomStock(
            "V03", 20.55, 20000, 20.60, 20000, 20.65, new ArrayList<Trade>());
        stocks.save(stock);

        URI postUri = new URI(baseUrl + port + "/api/stocks/" + stock.getSymbol());

        //Authority: User
        ResponseEntity<CustomStock> result = restTemplate.withBasicAuth("user_1", "password").getForEntity(postUri, CustomStock.class);

        //200 succesful return of stock
        assertEquals(200, result.getStatusCode().value());
    }
}