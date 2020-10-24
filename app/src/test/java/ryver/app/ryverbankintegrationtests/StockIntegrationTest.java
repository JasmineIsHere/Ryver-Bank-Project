package ryver.app.ryverbankintegrationtests;

import ryver.app.stock.StockRepository;
import ryver.app.stock.CustomStock;
import ryver.app.customer.*;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Optional;

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
        customers.deleteAll();
		stocks.deleteAll();
    }
    
    @Test
    public void getStockBySymbol_ROLEUser_Success() throws Exception{
        CustomStock stock = new CustomStock(
            "V03", 20.55, 20000, 20.60, 20000, 20.65, null);
        stocks.save(stock);

        System.out.println(stock.getSymbol());

        URI postUri = new URI(baseUrl + port + "/stocks/" + stock.getSymbol());

        ResponseEntity<CustomStock> result = restTemplate.withBasicAuth("manager_1", "01_manager_01").getForEntity(postUri, CustomStock.class);

        assertEquals(201, result.getStatusCode().value());
    }
    
    // @Test
    // public void getStockBySymbol_ROLEUser_Failure() throws Exception{
    //     URI uri = new URI(baseUrl + port + "/stocks/" + 1);

	// 	ResponseEntity<CustomStock> result = restTemplate.withBasicAuth("user_1", "password").getForEntity(uri, CustomStock.class);
		
	// 	//requested trade not found
	// 	assertEquals(404, result.getStatusCode().value());
    // }
}