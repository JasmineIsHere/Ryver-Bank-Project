// package ryver.app;

// import static org.junit.jupiter.api.Assertions.*;

// import java.net.URI;
// import java.util.Optional;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.web.server.LocalServerPort;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// import csd.week6.user.User;
// import csd.week6.user.UserRepository;

// /** Start an actual HTTP server listening at a random port*/
// @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// class CustomerAccountIntegrationTest {

// 	@LocalServerPort
// 	private int port;

// 	private final String baseUrl = "http://localhost:";

// 	@Autowired
// 	/**
// 	 * Use TestRestTemplate for testing a real instance of your application as an external actor.
// 	 * TestRestTemplate is just a convenient subclass of RestTemplate that is suitable for integration tests.
//  	 * It is fault tolerant, and optionally can carry Basic authentication headers.
// 	 */
// 	private TestRestTemplate restTemplate;

// 	@Autowired
// 	private AccountRepository accounts;

// 	@Autowired
// 	private CustomerRepository customers;

// 	@Autowired
// 	private BCryptPasswordEncoder encoder;

// 	@AfterEach
// 	void tearDown(){
// 		// clear the database after each test
// 		accounts.deleteAll();
// 		customers.deleteAll();
// 	}

// 	@Test
// 	public void getAccounts_Success() throws Exception {
// 		URI uri = new URI(baseUrl + port + "/accounts");
// 		books.save(new Account());
		
// 		// Need to use array with a ReponseEntity here
// 		ResponseEntity<Account[]> result = restTemplate.getForEntity(uri, Account[].class);
// 		Account[] accounts = result.getBody();
        
//         //JOLENE: idk how much of this is correct since its not books
// 		assertEquals(200, result.getStatusCode().value());
// 		assertEquals(1, accounts.length);
// 	}

// 	@Test
// 	public void getAccount_ValidBookId_Success() throws Exception {
// 		Account account = new Account();
// 		Long id = accounts.save(account).getId();
// 		URI uri = new URI(baseUrl + port + "/accounts/" + id);
		
// 		ResponseEntity<Account> result = restTemplate.getForEntity(uri, Account.class);
			
//         //JOLENE: idk how much of this is correct since its not books
// 		assertEquals(200, result.getStatusCode().value());
// 		assertEquals(book.getTitle(), result.getBody().getTitle());
// 	}

// 	@Test
// 	public void getAccount_InvalidBookId_Failure() throws Exception {
// 		URI uri = new URI(baseUrl + port + "/accounts/1");
		
// 		ResponseEntity<Book> result = restTemplate.getForEntity(uri, Account.class);
			
// 		assertEquals(404, result.getStatusCode().value());
// 	}

// 	@Test
// 	public void addAccount_Success() throws Exception {
// 		URI uri = new URI(baseUrl + port + "/accounts");
// 		Account Account = new Account("A New Hope");
// 		customers.save(
//             new Customer("Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true));

// 		ResponseEntity<Account> result = restTemplate.withBasicAuth("admin", "goodpassword")
// 										.postForEntity(uri, account, Account.class);
			
// 		assertEquals(201, result.getStatusCode().value());
// 		assertEquals(Account.getTitle(), result.getBody().getTitle());
// 	}

//     @Test
//     public void deleteAccount_ValidBookId_Success() throws Exception {
//         Account account = accunts.save(new Account());
//         URI uri = new URI(baseUrl + port + "/accounts/" + accounts.getId().longValue());
// 		customers.save(
//             new Customer("Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true));
        
//         ResponseEntity<Void> result = restTemplate.withBasicAuth("manager", "password")
//                                       .exchange(uri, HttpMethod.DELETE, null, Void.class);
//         assertEquals(200, result.getStatusCode().value());
//         // An empty Optional should be returned by "findById" after deletion
//         Optional<Book> emptyValue = Optional.empty();
//         assertEquals(emptyValue, accounts.findById(account.getId()));
//     }

//     @Test
//     public void deleteAccount_InvalidBookId_Failure() throws Exception {
//         URI uri = new URI(baseUrl + port + "/accounts/1");
// 		customers.save(
//             new Customer("Jolene", "password", "manager", "Jolene Loh", "T1234567Z", "12345678", "address", true));
        
//         ResponseEntity<Void> result = restTemplate.withBasicAuth("manager", "password")
//                                       .exchange(uri, HttpMethod.DELETE, null, Void.class);
      
//         assertEquals(404, result.getStatusCode().value());
//     }
// }
