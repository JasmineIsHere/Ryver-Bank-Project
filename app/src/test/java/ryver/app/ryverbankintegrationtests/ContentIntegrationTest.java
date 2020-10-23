// package ryver.app.ryverbankintegrationtests;

// import ryver.app.content.ContentRepository;
// import ryver.app.content.Content;
// import ryver.app.customer.*;

// import static org.junit.jupiter.api.Assertions.*;

// import java.net.URI;
// import java.util.LinkedHashMap;
// import java.util.Optional;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.web.server.LocalServerPort;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// /** Start an actual HTTP server listening at a random port */
// @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// class ContentIntegrationTest {

// 	@LocalServerPort
// 	private int port;

// 	private final String baseUrl = "http://localhost:";

// 	@Autowired
// 	private TestRestTemplate restTemplate;

// 	@Autowired
//     private ContentRepository contents;
    
//     @Autowired
//     private CustomerRepository customers;

// 	@Autowired
// 	private BCryptPasswordEncoder encoder;

// 	@AfterEach
// 	void tearDown() {
//         // clear the database after each test
//         customers.deleteAll();
// 		contents.deleteAll();
//     }
    
//     @Test
//     public void addContent_ROLEManager_Success() throws Exception{
//         Customer customer = customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 
//         // Content content = "The title of the advisory or news","The short summary of the content item", "The text of the content item", "https://link.to.externalcontent", false;

//         String createContentJSON = "{\"title\":\"The title of the advisory or news\",\"summary\":\"The short summary of the content item\",\"content\":\"The text of the content item\",\"link\":\"https://link.to.externalcontent\",\"approved\":"+ false +"}";
		
// 		HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
// 		HttpEntity<String> entity = new HttpEntity<>(createContentJSON, headers);
//         URI postUri = new URI(baseUrl + port + "/contents");

//         ResponseEntity<Content> result = restTemplate.withBasicAuth("manager_1", "01_manager_01")
//             .postForEntity(postUri, entity, Content.class);

//         assertEquals(201, result.getStatusCode().value());
//     }

//     // @Test
//     // public void addContent_ROLEAnalyst_Success() throws Exception{
//     //     customers.save(new Customer("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST", "Analyst One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

//     //     String createContentJSON = 
// 	// 	"{\"title\":\"The title of the advisory or news\",\"summary\":\"The short summary of the content item\",\"content\":\"The text of the content item\",\"link\":\"https://link.to.externalcontent\",\"approved\":"+ false +"}";
		
// 	// 	HttpHeaders headers = new HttpHeaders();
//     //     headers.setContentType(MediaType.APPLICATION_JSON);
// 	// 	HttpEntity<String> entity = new HttpEntity<>(createContentJSON, headers);
//     //     URI postUri = new URI(baseUrl + port + "/contents");

//     //     ResponseEntity<Content> result = restTemplate.withBasicAuth("analyst_1", "01_analyst_01").postForEntity(postUri, entity, Content.class);

//     //     assertEquals(201, result.getStatusCode().value());
//     // }

//     // @Test
//     // public void addContent_ROLECustomer_Failure() throws Exception{
// 	// 	Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh",
//     //             "T0046822Z", "82345678", "address", true)); 
        
//     //     String createContentJSON = 
//     //     "{\"title\":\"The title of the advisory or news\",\"summary\":\"The short summary of the content item\",\"content\":\"The text of the content item\",\"link\":\"https://link.to.externalcontent\",\"approved\":"+ false +"}";
            
//     //     HttpHeaders headers = new HttpHeaders();
//     //     headers.setContentType(MediaType.APPLICATION_JSON);
//     //     HttpEntity<String> entity = new HttpEntity<>(createContentJSON, headers);
//     //     URI postUri = new URI(baseUrl + port + "/contents");
    
//     //     ResponseEntity<Content> result = restTemplate.withBasicAuth("analyst_1", "01_analyst_01").postForEntity(postUri, entity, Content.class);

//     //     assertEquals(403, result.getStatusCode().value());
//     // }

// 	// @Test
// 	// public void getAccountByAccountIdAndCustomerId_ROLECustomer_Success() throws Exception {
//     //     customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); // the manager that will retrieve customer account
// 	// 	Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh",
//     //             "T0046822Z", "82345678", "address", true)); //target customer
        
// 	// 	String createAccountJSON = 
// 	// 	"{\"customer_id\":" + customer.getId() + ",\"balance\":" + 10000.0 + ",\"available_balance\":" + 10000.0 + "}";
		
// 	// 	HttpHeaders headers = new HttpHeaders();
//     //     headers.setContentType(MediaType.APPLICATION_JSON);
// 	// 	HttpEntity<String> entity = new HttpEntity<>(createAccountJSON, headers);
//     //     URI postUri = new URI(baseUrl + port + "/accounts");

//     //     ResponseEntity<Account> acc = restTemplate.withBasicAuth("manager_1", "01_manager_01").postForEntity(postUri, entity, Account.class);

//     //     URI getUri = new URI(baseUrl + port + "/accounts/" + acc.getBody().getId());

// 	// 	ResponseEntity<Account> result = restTemplate.withBasicAuth("User_1", "password").getForEntity(getUri, Account.class);


// 	// 	assertEquals(200, result.getStatusCode().value());
//     // }
// }