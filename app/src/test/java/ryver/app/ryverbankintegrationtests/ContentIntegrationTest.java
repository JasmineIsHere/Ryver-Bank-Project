package ryver.app.ryverbankintegrationtests;

import ryver.app.content.ContentRepository;
import ryver.app.content.Content;
import ryver.app.customer.*;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
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
        //create manager and content
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
        URI postUri = new URI(baseUrl + port + "/api/contents");

        //Authority: Manager
        ResponseEntity<Content> result = restTemplate.withBasicAuth("manager_1", "01_manager_01")
            .postForEntity(postUri, entity, Content.class);

        //return 201 succesful creation of article
        assertEquals(201, result.getStatusCode().value());
    }

    @Test
    public void addContent_ROLEAnalyst_Success() throws Exception{
        //create analyst and content
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
        URI postUri = new URI(baseUrl + port + "/api/contents");

        //Authority: Analyst
        ResponseEntity<Content> result = restTemplate.withBasicAuth("analyst_1", "01_analyst_01")
            .postForEntity(postUri, entity, Content.class);

        //Return 201 succesful creation of article
        assertEquals(201, result.getStatusCode().value());
    }

    @Test
    public void addContent_ROLECustomer_Failure() throws Exception{
        //create customer and content
		Customer customer = customers.save(new Customer("user_1", encoder.encode("01_user_01"), "ROLE_USER", "Jerry Loh",
                "T0046822Z", "82345678", "address", true)); 
        
        JSONObject requestParams = new JSONObject();
        requestParams.put("title", "The title of the article"); 
        requestParams.put("summary", "The summary of the article");
        requestParams.put("content", "The content of the article");
        requestParams.put("link",  "https://article.com/article1");
        requestParams.put("approved",  false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
        URI postUri = new URI(baseUrl + port + "/api/contents");

        //Authority: User
        ResponseEntity<Content> result = restTemplate.withBasicAuth("user_1", "01_user_01").postForEntity(postUri, entity, Content.class);

        //return 403 forbidden access, User cannot create article
        assertEquals(403, result.getStatusCode().value());
    }

    @Test
	public void updateContent_RoleManager_Success() throws Exception{
        //create manager and content
        Customer customer = customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

        Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
        contents.save(content);

        //updated content
		JSONObject requestParams = new JSONObject();
        requestParams.put("title", "The updated title of the article"); 
        requestParams.put("summary", "The updated summary of the article");
        requestParams.put("content", "The updated content of the article");
        requestParams.put("link",  "https://article.com/article1");
        requestParams.put("approved",  false);

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
		URI uri = new URI(baseUrl + port + "/api/contents/" + content.getId());

        //Authority: Manager
		ResponseEntity<Content> result = restTemplate.withBasicAuth("manager_1", "01_manager_01").exchange(uri, HttpMethod.PUT, entity, Content.class);

		//return 200 succesful update of content
		assertEquals(200, result.getStatusCode().value());
    }
    
    @Test
	public void updateContent_RoleAnalyst_Success() throws Exception{
        //create Analyst and content
        Customer customer = customers.save(new Customer("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST", "Analyst One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

        Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
        contents.save(content);

		//uodated content
		JSONObject requestParams = new JSONObject();
        requestParams.put("title", "The updated title of the article"); 
        requestParams.put("summary", "The updated summary of the article");
        requestParams.put("content", "The updated content of the article");
        requestParams.put("link",  "https://article.com/article1");
        requestParams.put("approved",  false);

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
        URI uri = new URI(baseUrl + port + "/api/contents/" + content.getId());
        
        //Authority: Analyst
		ResponseEntity<Content> result = restTemplate.withBasicAuth("analyst_1", "01_analyst_01").exchange(uri, HttpMethod.PUT, entity, Content.class);

        //return 200 succesful update of content
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
	public void updateContent_RoleUser_Failure() throws Exception{
        //create customer and content
 		Customer customer = customers.save(new Customer("user_1", encoder.encode("01_user_01"), "ROLE_USER", "Jerry Loh", "T0046822Z", "82345678", "address", true)); 

        Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
        contents.save(content);

        //updated content
		JSONObject requestParams = new JSONObject();
        requestParams.put("title", "The updated title of the article"); 
        requestParams.put("summary", "The updated summary of the article");
        requestParams.put("content", "The updated content of the article");
        requestParams.put("link",  "https://article.com/article1");
        requestParams.put("approved",  false);

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestParams.toJSONString(), headers);
		URI uri = new URI(baseUrl + port + "/api/contents/" + content.getId());

        //Authority: User
		ResponseEntity<Content> result = restTemplate.withBasicAuth("user_1", "01_user_01").exchange(uri, HttpMethod.PUT, entity, Content.class);
 
        //return 403 forbidden as User cannot update content
		assertEquals(403, result.getStatusCode().value());
    }

    @Test
    public void deleteContent_RoleManager_Success() throws Exception {
        //create manager and content
        Customer customer = customers.save(new Customer("manager_1", encoder.encode("01_manager_01"), "ROLE_MANAGER", "Manager One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

        Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
        contents.save(content);

        URI uri = new URI(baseUrl + port + "/api/contents/" + content.getId());
        
        //Authority: Manager
        ResponseEntity<Void> result = restTemplate.withBasicAuth("manager_1", "01_manager_01").exchange(uri, HttpMethod.DELETE, null, Void.class);

        //return 200 successful deletion of content
        assertEquals(200, result.getStatusCode().value());
        
        // An empty Optional should be returned by "findById" after deletion
        Optional<Content> emptyValue = Optional.empty();
        assertEquals(emptyValue, contents.findById(content.getId()));
    }

    @Test
    public void deleteContent_RoleAnalysr_Success() throws Exception {
        //create analyst and content
        Customer customer = customers.save(new Customer("analyst_1", encoder.encode("01_analyst_01"), "ROLE_ANALYST", "Analyst One","S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true)); 

        Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
        contents.save(content);

        URI uri = new URI(baseUrl + port + "/api/contents/" + content.getId());
        
        //Authority: analyst
        ResponseEntity<Void> result = restTemplate.withBasicAuth("analyst_1", "01_analyst_01").exchange(uri, HttpMethod.DELETE, null, Void.class);

        //return 200 successful deletion of content
        assertEquals(200, result.getStatusCode().value());
        
        // An empty Optional should be returned by "findById" after deletion
        Optional<Content> emptyValue = Optional.empty();
        assertEquals(emptyValue, contents.findById(content.getId()));
    }

    @Test
    public void deleteContent_RoleUser_Failure() throws Exception {
        //create customer and content
 		Customer customer = customers.save(new Customer("user_1", encoder.encode("01_user_01"), "ROLE_USER", "Jerry Loh", "T0046822Z", "82345678", "address", true));

        Content content = new Content("The title of the article", "The summary of the article", "The content of the article", "https://article.com/article1", false);
        contents.save(content);

        URI uri = new URI(baseUrl + port + "/api/contents/1");
        
        //Authority: customer
        ResponseEntity<Void> result = restTemplate.withBasicAuth("user_1", "01_user_01").exchange(uri, HttpMethod.DELETE, null, Void.class);
      
        //return 403 forbidden customer cannot delete content
        assertEquals(403, result.getStatusCode().value());
    }
}

