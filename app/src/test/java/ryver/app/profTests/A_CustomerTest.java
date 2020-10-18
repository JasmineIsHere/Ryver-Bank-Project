// package ryver.app.proftests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.path.json.config.JsonPathConfig;
import net.minidev.json.JSONObject;

import static org.hamcrest.Matchers.*;
import static io.restassured.config.RedirectConfig.redirectConfig;

import static io.restassured.RestAssured.*;

@TestMethodOrder(OrderAnnotation.class)
public class A_CustomerTest {
    /**
        Customer info (in JSON):
        {
            "id": (auto-generated by your api, int value),
            "full_name":"Mark Tan",
            "nric":"S8529649C", (valid nric number starting with S or T and valid checksum)
            "phone":"91251234", (valid SG phone - 8 digits starting with 6, 8 or 9)
            "address":"27 Jalan Alamak S680234", (string, no need validation)
            "username":"gooduser",
            "password":"password" (need to hash password),
            "authorities":"ROLE_USER",
            "active": true (or false: to indicate if the customer account is in use)
        }
     * 
     */
    @BeforeAll
    public static void initClass() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
        RestAssured.config = RestAssured.config()
            .jsonConfig(JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE))
            .redirect(redirectConfig().followRedirects(false));
    }
    private String nric1 = "S8529649C";
    private String nric_invalid = "S5296491C";

     /**
     * Add the 1st user successfully.
     * @throws Exception
     */
    @Test
    @Order(1)
    public void testAddUser_Valid_ReturnJson() throws Exception{
        JSONObject requestParams = new JSONObject();
        requestParams.put("full_name", "Mark Tan"); 
        // nric must be unique and follow the Singapore NRIC format
        requestParams.put("nric", nric1);
        // valid Singapore number
        requestParams.put("phone", "93223235");
        requestParams.put("address", "27 Jalan Alamak S680234");
        // unique username
        requestParams.put("username", TestConstants.u1_USERNAME);
        requestParams.put("password", TestConstants.u1_PASSWORD);
        requestParams.put("authorities", "ROLE_USER");
        requestParams.put("active", true);
            
        TestConstants.user_id_1 = 
                given().auth().basic(TestConstants.m_USERNAME, TestConstants.m_PASSWORD)
                .accept("*/*")
                .contentType("application/json")
                .body(requestParams.toJSONString())
                .post(TestConstants.customerURL)
                .then()
                .statusCode(201)
                .body(containsString(nric1))
                .extract().path("id");
        // view by this user - successful
        given().auth().basic(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD)
                .accept("*/*")
                .contentType("application/json")
                .get(TestConstants.customerURL + "/" + TestConstants.user_id_1)
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("nric", equalTo(nric1));
    }

    /**
     * Test add user with invalid data, e.g., invalid nric or phone
     * Return 400 - bad request
     * @throws Exception
     */
    @Test
    @Order(2)
    public void testAddUser_Invalid_Return400() throws Exception{
        JSONObject requestParams = new JSONObject();
        requestParams.put("full_name", "Mark Tan"); 
        requestParams.put("nric", nric_invalid);
        requestParams.put("phone", "2435");
        requestParams.put("address", "27 Jalan Alamak S680234");
        requestParams.put("username", "user1");
        requestParams.put("password", "pwd1");
        requestParams.put("authorities", "ROLE_USER");
        requestParams.put("active", true);
            
        given().auth().basic(TestConstants.m_USERNAME, TestConstants.m_PASSWORD)
                .accept("*/*")
                .contentType("application/json")
                .body(requestParams.toJSONString())
                .post(TestConstants.customerURL)
                .then()
                .statusCode(400);
        
    }
    
    
    /**
     * Test update info by user1.
     * @throws Exception
     */
    @Test
    @Order(3)
    public void testUpdateUser_Valid_ReturnJson() throws Exception{
        JSONObject requestParams = new JSONObject();
        // the user would provide all field values in the update
        // but these fields should be ignored
        requestParams.put("full_name", "John Tan"); 
        requestParams.put("username", TestConstants.u1_USERNAME);
        requestParams.put("nric", "S5042165A");
        requestParams.put("authorities", "ROLE_USER");
        requestParams.put("active", true);

        // can update phone, address, and password only
        requestParams.put("phone", "99992222");
        requestParams.put("address", "001 Jalan Alamak S680234");
        // but this test chooses not to update password
        requestParams.put("password", TestConstants.u1_PASSWORD);
        
        given().auth().basic(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD)
                .accept("*/*")
                .contentType("application/json")
                .body(requestParams.toJSONString())
                .put(TestConstants.customerURL+ "/" + TestConstants.user_id_1)
                .then()
                .statusCode(200)
                .body("phone", equalTo("99992222"))
                .body("nric", equalTo(nric1));
    }

    // There can be more similar tests for username/nric conflict, update, deactivate, etc.
}
