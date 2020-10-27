package ryver.app.ryverbankintegrationtests;

import ryver.app.customer.CustomerRepository;
import ryver.app.customer.Customer;

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
class CustomerIntegrationTest {

	@LocalServerPort
	private int port;

	private final String baseUrl = "http://localhost:";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private CustomerRepository customers;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@AfterEach
	void tearDown() {
		// clear the database after each test
		customers.deleteAll();
	} 

	@Test
	public void addCustomer_Success() throws Exception {
		// Customer newCustomer = new Customer("Jolene", encoder.encode("new_password"), "ROLE_USER", "Jolene Loh",
		// 		"T0046822Z", "97123456", "updated_address", true);

		String newCustomer = 
		"{\"username\":\"good_user_1\",\"password\":\"01_user_01\",\"authorities\":\"ROLE_USER\",\"full_name\":\"User One\", \"nric\":\"S9942296C\",\"phone\":\"90123456\",\"address\":\"999 Tampines Road S99999\", \"active\": true}";
		customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); // the manager that will be updated the customer details
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(newCustomer, headers);
		//System.out.println(entity);
		URI uri = new URI(baseUrl + port + "/customers");

		ResponseEntity<LinkedHashMap> result = restTemplate.withBasicAuth("manager_1", "01_manager_01").postForEntity(uri, entity, LinkedHashMap.class);
		//ResponseEntity<Customer> result = restTemplate.withBasicAuth("manager_1", "01_manager_01").postForEntity(uri, newCustomer, Customer.class);

		assertEquals(201, result.getStatusCode().value());
	}

	@Test
	public void addCustomer_ROLEUser_Failure() throws Exception {

		String newCustomer = 
		"{\"username\":\"good_user_1\",\"password\":\"01_user_01\",\"authorities\":\"ROLE_USER\",\"full_name\":\"User One\", \"nric\":\"S9942296C\",\"phone\":\"90123456\",\"address\":\"999 Tampines Road S99999\", \"active\": true}";
		customers.save(new Customer("user_2", encoder.encode("02_user_02"), "ROLE_USER", "User One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); // the manager that will be updated the customer details
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(newCustomer, headers);
		URI uri = new URI(baseUrl + port + "/customers");

		ResponseEntity<LinkedHashMap> result = restTemplate.withBasicAuth("user_2", "02_user_02").postForEntity(uri, entity, LinkedHashMap.class);
		
		assertEquals(403, result.getStatusCode().value()); //Forbidden
	}

	@Test
	public void updateCustomer_ManagerUpdateCustomer_Success() throws Exception {
		customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One",
				"S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); // the manager that will be updated the
																				// customer details

		Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh",
				"T0046822Z", "82345678", "address", true)); //original customer
		URI uri = new URI(baseUrl + port + "/customers/" + customer.getId());

		String updatedCustomer = 
		"{\"username\":\"good_user_1\",\"password\":\"01_user_01\",\"authorities\":\"ROLE_USER\",\"full_name\":\"User One\", \"nric\":\"S9942296C\",\"phone\":\"90123456\",\"address\":\"999 Tampines Road S99999\", \"active\": true}";
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(updatedCustomer, headers);

		ResponseEntity<LinkedHashMap> result = restTemplate.withBasicAuth("manager_1", "01_manager_01").exchange(uri,
				HttpMethod.PUT, entity, LinkedHashMap.class);
	
		assertEquals(200, result.getStatusCode().value());

		// assertEquals("999 Tampines Road S99999", result.getBody().get("address"));
		// assertEquals(updatedCustomer.getPhone(), result.getBody().get("phone"));
		// assertEquals(updatedCustomer.getPassword(), result.getBody().get("password"));
		// assertEquals("false", result.getBody().get("active"));

		// assertNotEquals(updatedCustomer.getUsername(), result.getBody().getUsername());
		// assertNotEquals(updatedCustomer.getFullName(), result.getBody().getFullName());
		// assertNotEquals(updatedCustomer.getNric(), result.getBody().getNric());
		// assertNotEquals(updatedCustomer.getAuthorities(), result.getBody().getAuthorities());
	}

	@Test
	public void updateCustomer_ManagerInactivateCustomer_Success() throws Exception {
		customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One",
				"S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); // the manager that will be updated the
																				// customer details

		Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh",
				"T0046822Z", "82345678", "address", true)); //original customer
		URI uri = new URI(baseUrl + port + "/customers/" + customer.getId());

		String updatedCustomer = 
		"{\"username\":\"good_user_1\",\"password\":\"01_user_01\",\"authorities\":\"ROLE_USER\",\"full_name\":\"User One\", \"nric\":\"S9942296C\",\"phone\":\"90123456\",\"address\":\"999 Tampines Road S99999\", \"active\": false}";
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(updatedCustomer, headers);

		ResponseEntity<LinkedHashMap> result = restTemplate.withBasicAuth("manager_1", "01_manager_01").exchange(uri,
				HttpMethod.PUT, entity, LinkedHashMap.class);
		
		assertEquals(200, result.getStatusCode().value());
		assertEquals(false, result.getBody().get("active"));
	}

	@Test
	public void updateCustomer_CustomerUpdateItself_Success() throws Exception {																				// customer details

		Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh",
				"T0046822Z", "82345678", "address", true)); //original customer
		URI uri = new URI(baseUrl + port + "/customers/" + customer.getId());

		String updatedCustomer = 
		"{\"username\":\"good_user_1\",\"password\":\"01_user_01\",\"authorities\":\"ROLE_USER\",\"full_name\":\"User One\", \"nric\":\"S9942296C\",\"phone\":\"90123456\",\"address\":\"999 Tampines Road S99999\", \"active\": false}";
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(updatedCustomer, headers);

		ResponseEntity<LinkedHashMap> result = restTemplate.withBasicAuth("User_1", "password").exchange(uri,
				HttpMethod.PUT, entity, LinkedHashMap.class);
		
		assertEquals(200, result.getStatusCode().value());
		// assertEquals(updatedCustomer.getAddress(), result.getBody().getAddress());
		// assertEquals(updatedCustomer.getPhone(), result.getBody().getPhone());
		// assertEquals(updatedCustomer.getPassword(), result.getBody().getPassword());
		// assertEquals(updatedManager.isActive(), savedManager.isActive()); //still
		// can't be tested

		// assertNotEquals(updatedCustomer.getUsername(), result.getBody().getUsername());
		// assertNotEquals(updatedCustomer.getFullName(), result.getBody().getFullName());
		// assertNotEquals(updatedCustomer.getNric(), result.getBody().getNric());
		// assertNotEquals(updatedCustomer.getAuthorities(), result.getBody().getAuthorities());
	}
	@Test
	public void updateCustomer_CustomerInactivateItself_Ignored() throws Exception {																				// customer details

		Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh",
				"T0046822Z", "82345678", "address", true)); //original customer
		URI uri = new URI(baseUrl + port + "/customers/" + customer.getId());

		String updatedCustomer = 
		"{\"username\":\"good_user_1\",\"password\":\"01_user_01\",\"authorities\":\"ROLE_USER\",\"full_name\":\"User One\", \"nric\":\"S9942296C\",\"phone\":\"90123456\",\"address\":\"999 Tampines Road S99999\", \"active\": false}";
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(updatedCustomer, headers);

		ResponseEntity<LinkedHashMap> result = restTemplate.withBasicAuth("User_1", "password").exchange(uri,
				HttpMethod.PUT, entity, LinkedHashMap.class);
		System.out.println("CUSTOMER: " + result.getBody());
		System.out.println("Users: " + customers.count());

		
		assertEquals(200, result.getStatusCode().value());
		assertEquals(true, result.getBody().get("active"));
	}

	@Test
	public void updateCustomer_CustomerUpdateOtherCustomer_Failure() throws Exception {																				// customer details

		Customer customer = customers.save(new Customer("User_1", encoder.encode("password"), "ROLE_USER", "Jerry Loh",
				"T0046822Z", "82345678", "address", true)); //original customer
		customers.save(new Customer("User_2", encoder.encode("password"), "ROLE_USER", "Jessica", "S1234567D", "91234567", "address2", true)); //other customer
		URI uri = new URI(baseUrl + port + "/customers/" + customer.getId());

		String updatedCustomer = 
		"{\"username\":\"good_user_1\",\"password\":\"01_user_01\",\"authorities\":\"ROLE_USER\",\"full_name\":\"User One\", \"nric\":\"S9942296C\",\"phone\":\"90123456\",\"address\":\"999 Tampines Road S99999\", \"active\": false}";
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(updatedCustomer, headers);

		ResponseEntity<LinkedHashMap> result = restTemplate.withBasicAuth("User_2", "password").exchange(uri,
				HttpMethod.PUT, entity, LinkedHashMap.class); //User_2 tries to update User_1

		
		assertEquals(403, result.getStatusCode().value());
	}
}