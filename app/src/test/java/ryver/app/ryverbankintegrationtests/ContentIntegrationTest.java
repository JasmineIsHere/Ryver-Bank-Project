package ryver.app.ryverbankintegrationtests;

import ryver.app.content.ContentRepository;
import ryver.app.content.Content;
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
class ContentIntegrationTest {

	@LocalServerPort
	private int port;

	private final String baseUrl = "http://localhost:";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
    private ContentRepository contents;
    
    @Autowired
    private CustomerRepository customers;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@AfterEach
	void tearDown() {
        // clear the database after each test
        customers.deleteAll();
		contents.deleteAll();
    }
    
    @Test
    public void addContent_ROLEManager_Success() throws Exception{
        Customer customer = customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

        JSONObject requestParams = new JSONObject();
        requestParams.put("title", "The title of the article"); 
        requestParams.put("summary", "The summary of the article");
        requestParams.put("content", "The content of the article");
        requestParams.put("link",  "https://article.com/article1");
        requestParams.put("approved",  false);

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
        URI postUri = new URI(baseUrl + port + "/contents");

        ResponseEntity<Content> result = restTemplate.withBasicAuth("manager_1", "01_manager_01")
            .postForEntity(postUri, entity, Content.class);

        assertEquals(201, result.getStatusCode().value());
    }

    @Test
    public void addContent_ROLEAnalyst_Success() throws Exception{
        Customer customer = customers.save(new Customer("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST", "Analyst One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

        JSONObject requestParams = new JSONObject();
        requestParams.put("title", "The title of the article"); 
        requestParams.put("summary", "The summary of the article");
        requestParams.put("content", "The content of the article");
        requestParams.put("link",  "https://article.com/article1");
        requestParams.put("approved",  false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
        URI postUri = new URI(baseUrl + port + "/contents");

        ResponseEntity<Content> result = restTemplate.withBasicAuth("analyst_1", "01_analyst_01")
            .postForEntity(postUri, entity, Content.class);

        assertEquals(201, result.getStatusCode().value());
    }

    // @Test
    // public void addContent_ROLECustomer_Failure() throws Exception{
	// 	Customer customer = customers.save(new Customer("user_1", encoder.encode("01_user_01"), "ROLE_USER", "Jerry Loh",
    //             "T0046822Z", "82345678", "address", true)); 
        
    //     JSONObject requestParams = new JSONObject();
    //     requestParams.put("title", "The title of the article"); 
    //     requestParams.put("summary", "The summary of the article");
    //     requestParams.put("content", "The content of the article");
    //     requestParams.put("link",  "https://article.com/article1");
    //     requestParams.put("approved",  false);

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
    //     URI postUri = new URI(baseUrl + port + "/contents");

    //     ResponseEntity<Content> result = restTemplate.withBasicAuth("user_1", "01_user_01")
    //         .postForEntity(postUri, entity, Content.class);

    //     assertEquals(403, result.getStatusCode().value());
    // }

    // @Test
    // public void getContent_ROLEUser_Success() throws Exception{
    //     Customer customer = new Customer("user_1", encoder.encode("password"), "ROLE_USER", "user_fullname", "S7812345A", "91234567", "address", true);
    //     customer.setId(1L);
    //     customers.save(customer);

    //     Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
    //     contents.save(content);
    
    //     URI postUri = new URI(baseUrl + port + "/contents");

    //     ResponseEntity<Content> result = restTemplate.withBasicAuth("user_1", "password").getForEntity(postUri, Content.class);

    //     assertEquals(200, result.getStatusCode().value());
    // }

    @Test
	public void updateContent_RoleManager_Success() throws Exception{
        Customer customer = customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

        Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
        contents.save(content);

		//custmer cancels trade 
		JSONObject requestParams = new JSONObject();
        requestParams.put("title", "The updated title of the article"); 
        requestParams.put("summary", "The updated summary of the article");
        requestParams.put("content", "The updated content of the article");
        requestParams.put("link",  "https://article.com/article1");
        requestParams.put("approved",  false);

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
		URI uri = new URI(baseUrl + port + "/contents/" + content.getId());

		ResponseEntity<Content> result = restTemplate.withBasicAuth("manager_1", "01_manager_01").exchange(uri, HttpMethod.PUT, entity, Content.class);

		//requested trade updated
		assertEquals(200, result.getStatusCode().value());
    }
    
    @Test
	public void updateContent_RoleAnalyst_Success() throws Exception{
        Customer customer = customers.save(new Customer("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST", "Analyst One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

        Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
        contents.save(content);

		//custmer cancels trade 
		JSONObject requestParams = new JSONObject();
        requestParams.put("title", "The updated title of the article"); 
        requestParams.put("summary", "The updated summary of the article");
        requestParams.put("content", "The updated content of the article");
        requestParams.put("link",  "https://article.com/article1");
        requestParams.put("approved",  false);

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
		URI uri = new URI(baseUrl + port + "/contents/" + content.getId());

		ResponseEntity<Content> result = restTemplate.withBasicAuth("analyst_1", "01_analyst_01").exchange(uri, HttpMethod.PUT, entity, Content.class);

		//requested trade updated
		assertEquals(200, result.getStatusCode().value());
    }

    // @Test
	// public void updateContent_RoleUser_Failure() throws Exception{
 	// 	Customer customer = customers.save(new Customer("user_1", encoder.encode("01_user_01"), "ROLE_USER", "Jerry Loh",
    //             "T0046822Z", "82345678", "address", true)); 

    //     Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
    //     contents.save(content);

	// 	//custmer cancels trade 
	// 	JSONObject requestParams = new JSONObject();
    //     requestParams.put("title", "The updated title of the article"); 
    //     requestParams.put("summary", "The updated summary of the article");
    //     requestParams.put("content", "The updated content of the article");
    //     requestParams.put("link",  "https://article.com/article1");
    //     requestParams.put("approved",  false);

	// 	HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
	// 	HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
	// 	URI uri = new URI(baseUrl + port + "/contents/" + content.getId());

	// 	ResponseEntity<Content> result = restTemplate.withBasicAuth("user_1", "01_user_01").exchange(uri, HttpMethod.PUT, entity, Content.class);

	// 	//requested trade updated
	// 	assertEquals(403, result.getStatusCode().value());
    // }
}