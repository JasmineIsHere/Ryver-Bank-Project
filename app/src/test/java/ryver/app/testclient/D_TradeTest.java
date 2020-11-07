package ryver.app.testclient;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import net.minidev.json.JSONObject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import static io.restassured.config.RedirectConfig.redirectConfig;

import static io.restassured.RestAssured.*;

@TestMethodOrder(OrderAnnotation.class)
public class D_TradeTest {

   /**
     * Endpoint: baseURL + "/trades"
     * All numbers are double except quantity/volume (int) and timestamp (long).
     * 
     * Trade info:
        {
            "id": (auto-generated by your api),
            "action":"buy", (specify "sell" for selling)
            "symbol":"A17U",
            "quantity":1000, (in multiples of 100)
            "bid":3.28, (specify 0.0 for market order, ignored if action is "sell")
            "ask":3.27, (specify 0.0 for market order, ignored if action is "buy")
            "avg_price":3.30 (the average filled price, as one trade can be matched by several other trades)
            "filled_quantity":0;
            "date": (submitted time in Unix timestamp, expired after 5pm of the day),
            "account_id":1234, (account to debit or to deposit fund)
            "customer_id":1234, (submitter of the trade)
            "status":"open" (or "filled", "partial-filled", "cancelled", "expired")
        }
     */
    private final String stockToTrade = "S58"; // buy and sell this stock
    private final String stockToBuy = "C6L"; // 2nd stock to buy, not sell
    private final String stockToShort = "Z74";
    private final String stockToContra = "D05";
    public final int buyQuantity = 2000;
    public final int sellQuantity = 1000;
    public final int minQuantity = 100;

    public static int boughtQuantity_1 = 0;
    public static int boughtQuantity_2 = 0;
    public static double trade_balance_1 = TestConstants.account_balance_1;
    public static double trade_balance_2 = TestConstants.account_balance_2;
    
    @BeforeAll
    public static void initClass() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
        RestAssured.config = RestAssured.config()
            .jsonConfig(JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE))
            .redirect(redirectConfig().followRedirects(false));
    }

    /**
     * Utility methods
     * 
     */
    private Response getStockInfo(String stock) {
        return given().auth().basic(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD)
                .accept("*/*").contentType("application/json")
                .get(TestConstants.stockURL + "/" + stock)
                .then().statusCode(200).extract().response();
    }       

    private String createTradeRequestBody(
            String action, String symbol, int quantity, double bid, 
            double ask, int accountId, int userId, String status){

        JSONObject requestParams = new JSONObject();
        requestParams.put("action", action);
        requestParams.put("symbol", symbol);
        requestParams.put("quantity", quantity);
        requestParams.put("bid", bid);
        requestParams.put("ask", ask);
        requestParams.put("account_id", accountId);
        requestParams.put("customer_id", userId);
        requestParams.put("status", status);

        return requestParams.toJSONString();
    }

    private Response createTrade(String username, String password, String body){
        return given().auth().basic(username, password)
                .accept("*/*").contentType("application/json")
                .body(body).post(TestConstants.tradeURL)
                .then().extract().response();
    }
    
    private Response updateTrade(String username, String password, String body, int id){
        return given().auth().basic(username, password)
                .accept("*/*").contentType("application/json")
                .body(body).put(TestConstants.tradeURL + "/" + id)
                .then().extract().response();
    }

    private Response getAccount(String username, String password, int accountId){
        return given().auth().basic(username, password)
                .accept("*/*").contentType("application/json")
                .get(TestConstants.accountURL + "/" + accountId)
                .then().statusCode(200).extract().response();
    }

    private Response getPortfolio(String username, String password){
        return given().auth().basic(username, password)
                .accept("*/*").contentType("application/json")
                .get(TestConstants.portfolioURL)
                .then().statusCode(200).extract().response();
    }

    private void assertTrade(Response trade, int code, String status, double avg_price, int filled_quantity, String symbol){
        assertEquals(code, trade.getStatusCode());
        assertEquals(status, trade.jsonPath().getString("status"));
        assertEquals(avg_price, trade.jsonPath().getDouble("avg_price"), 0.001);
        assertEquals(filled_quantity, trade.jsonPath().getInt("filled_quantity"));
        assertEquals(symbol, trade.jsonPath().getString("symbol"));
    }

    /**
     * Test buying at market price - filled right away.
     * Your API should initialize the bid/ask volumes of each stock to 20000 at the start.
     * @throws Exception
     */
    @Test
    @Order(1)
    public void testBuy_Success() throws Exception{
        Response stockInfo = getStockInfo(stockToTrade);
        double ask_price = stockInfo.jsonPath().getDouble("ask");
        int ask_volume = stockInfo.jsonPath().getInt("ask_volume");

        String requestBody = createTradeRequestBody("buy", stockToTrade, buyQuantity, 
                0, 0, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        
        assertTrade(tradeInfo, 201, "filled", ask_price, buyQuantity, stockToTrade);
        
        // update global variables
        trade_balance_1 -= ask_price * buyQuantity;
        boughtQuantity_1 += buyQuantity;
        
        // check balance of account
        Response accountInfo = getAccount(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, TestConstants.account_id_1);
        assertEquals(trade_balance_1, accountInfo.jsonPath().getDouble("balance"), 0.01);

        // check updated last_price, volume, etc. in stock info
        stockInfo = getStockInfo(stockToTrade);
        assertEquals(ask_price, stockInfo.jsonPath().getDouble("last_price"));
        assertEquals(ask_volume - buyQuantity, stockInfo.jsonPath().getInt("ask_volume"));
    }

    @Test
    @Order(2)
    public void testBuyLimit_Success() throws Exception{
        Response stockInfo = getStockInfo(stockToTrade);
        double ask_price = stockInfo.jsonPath().getDouble("ask");
        int ask_volume = stockInfo.jsonPath().getInt("ask_volume");

        // limit buy at price > ask_price
        String requestBody = createTradeRequestBody("buy", stockToTrade, buyQuantity, 
                ask_price*2, 0, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        
        // it should be filled at ask_price
        assertTrade(tradeInfo, 201, "filled", ask_price, buyQuantity, stockToTrade);
        
        // update global variables
        trade_balance_1 -= ask_price * buyQuantity;
        boughtQuantity_1 += buyQuantity;
        
        // check balance of account
        Response accountInfo = getAccount(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, TestConstants.account_id_1);
        assertEquals(trade_balance_1, accountInfo.jsonPath().getDouble("balance"), 0.01);

        // check updated last_price, volume, etc. in stock info
        stockInfo = getStockInfo(stockToTrade);
        assertEquals(ask_price, stockInfo.jsonPath().getDouble("last_price"));
        assertEquals(ask_volume - buyQuantity, stockInfo.jsonPath().getInt("ask_volume"));
    }
    
    /**
     * Wipe out the volume of stockToTrade.
     */
    @Test
    @Order(3)
    public void testBuyAllVolume() throws Exception{
        Response stockInfo = getStockInfo(stockToTrade);
        double ask_price = stockInfo.jsonPath().getDouble("ask");
        int ask_volume = stockInfo.jsonPath().getInt("ask_volume");

        String requestBody = createTradeRequestBody("buy", stockToTrade, ask_volume, 
                0, 0, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        
        assertTrade(tradeInfo, 201, "filled", ask_price, ask_volume, stockToTrade);

        // update global variables
        trade_balance_1 -= ask_price * ask_volume;
        boughtQuantity_1 += ask_volume;
        
        // check balance of account 1
        Response accountInfo = getAccount(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, TestConstants.account_id_1);
        assertEquals(trade_balance_1, accountInfo.jsonPath().getDouble("balance"));

        // check updated last_price, volume, etc. in stock info
        stockInfo = getStockInfo(stockToTrade);
        assertEquals(0, stockInfo.jsonPath().getInt("ask_volume"));
    }

    
    /**
     * Test selling at market price, and filled immediately.
     * @throws Exception
     */
    @Test
    @Order(4)
    public void testSell_Success() throws Exception{
        Response stockInfo = getStockInfo(stockToTrade);
        double bid_price = stockInfo.jsonPath().getDouble("bid");
        int bid_volume = stockInfo.jsonPath().getInt("bid_volume");
        
        String requestBody = createTradeRequestBody("sell", stockToTrade, sellQuantity, 
                0, 0, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        
        assertTrade(tradeInfo, 201, "filled", bid_price, sellQuantity, stockToTrade);

        // update the bought quantity and check balance
        boughtQuantity_1 -= sellQuantity;
        trade_balance_1 += bid_price * sellQuantity;
        Response accountInfo = getAccount(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, TestConstants.account_id_1);
        assertEquals(trade_balance_1, accountInfo.jsonPath().getDouble("balance"));

        // check updated last_price, volume, etc. in stock info
        stockInfo = getStockInfo(stockToTrade);
        assertEquals(bid_price, stockInfo.jsonPath().getDouble("last_price"));
        assertEquals(bid_volume - sellQuantity, stockInfo.jsonPath().getInt("bid_volume"));
    }

    @Test
    @Order(5)
    public void testSellLimit_Success() throws Exception{
        Response stockInfo = getStockInfo(stockToTrade);
        double bid_price = stockInfo.jsonPath().getDouble("bid");
        int bid_volume = stockInfo.jsonPath().getInt("bid_volume");
        
        // limit sell which is cheaper that current bid
        String requestBody = createTradeRequestBody("sell", stockToTrade, sellQuantity, 
                0, bid_price - 1, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        
        // it is matched at current bid
        assertTrade(tradeInfo, 201, "filled", bid_price, sellQuantity, stockToTrade);

        // update the bought quantity and check balance
        boughtQuantity_1 -= sellQuantity;
        trade_balance_1 += bid_price * sellQuantity;
        Response accountInfo = getAccount(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, TestConstants.account_id_1);
        assertEquals(trade_balance_1, accountInfo.jsonPath().getDouble("balance"));

        // check updated last_price, volume, etc. in stock info
        stockInfo = getStockInfo(stockToTrade);
        assertEquals(bid_price, stockInfo.jsonPath().getDouble("last_price"));
        assertEquals(bid_volume - sellQuantity, stockInfo.jsonPath().getInt("bid_volume"));
    }

    /**
     * Test buy/sell open orders - can't be filled to due high/low price.
     * Test cancel orders
     */
    @Test
    @Order(6)
    public void testBuySell_Open() throws Exception{
        // create open sell trade - note that your api has to determine the correct status for the trade
        // any status supplied by client should be ignored for trade creation
        double high_price = 50.0;
        String requestBody = createTradeRequestBody("sell", stockToTrade, sellQuantity, 
                0, high_price, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        assertTrade(tradeInfo, 201, "open", 0, 0, stockToTrade);

        // create open buy trade
        double low_price = 0.5;
        requestBody = createTradeRequestBody("buy", stockToTrade, buyQuantity, 
                low_price, 0, TestConstants.account_id_2, TestConstants.user_id_2, "");
        tradeInfo = createTrade(TestConstants.u2_USERNAME, TestConstants.u2_PASSWORD, requestBody);
        assertTrade(tradeInfo, 201, "open", 0, 0, stockToTrade);

        // cancel both open buy trades - supply the status "cancelled" via a put request
        int id = tradeInfo.jsonPath().getInt("id");
        requestBody = createTradeRequestBody("buy", stockToTrade, buyQuantity, 
                low_price, 0, TestConstants.account_id_2, TestConstants.user_id_2, "cancelled");
        tradeInfo = updateTrade(TestConstants.u2_USERNAME, TestConstants.u2_PASSWORD, requestBody, id);
        assertTrade(tradeInfo, 200, "cancelled", 0, 0, stockToTrade);

        // check balance of account 1
        Response accountInfo = getAccount(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, TestConstants.account_id_1);
        assertEquals(trade_balance_1, accountInfo.jsonPath().getDouble("balance"));
        
        // check balance of account 2
        accountInfo = getAccount(TestConstants.u2_USERNAME, TestConstants.u2_PASSWORD, TestConstants.account_id_2);
        assertEquals(trade_balance_2, accountInfo.jsonPath().getDouble("balance"));
        
    }

    @Test
    @Order(7)
    public void testComplexMatchingTrade() throws Exception{
        // initial volume of 20k for stockToTrade has been wiped out
        // there is already one open limit sell with high_price ($50) by user1 based on the above test case
        // now create new sell trade with a reasonable price of 5.0
        double good_price = 5.0, high_price = 50.0;
        String requestBody = createTradeRequestBody("sell", stockToTrade, sellQuantity, 
                0, good_price, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        assertTrade(tradeInfo, 201, "open", 0, 0, stockToTrade);

        // check updated stock information
        Response stockInfo = getStockInfo(stockToTrade);
        assertEquals(good_price, stockInfo.jsonPath().getDouble("ask"));
        assertEquals(sellQuantity, stockInfo.jsonPath().getInt("ask_volume"));

        // user2 submit a market buy with enough account balance (which is 10k) assuming $5 bid_price
        // this trade of user2 will be partial filled due to insufficient account balance
        requestBody = createTradeRequestBody("buy", stockToTrade, buyQuantity, 
                0, 0, TestConstants.account_id_2, TestConstants.user_id_2, "");
        tradeInfo = createTrade(TestConstants.u2_USERNAME, TestConstants.u2_PASSWORD, requestBody);

        // update data for user2
        boughtQuantity_2 = sellQuantity + minQuantity;
        double amount = sellQuantity * good_price + minQuantity * high_price;
        double avg_price = amount / boughtQuantity_2;
        trade_balance_2 -= amount;
        assertTrade(tradeInfo, 201, "partial-filled", avg_price, boughtQuantity_2, stockToTrade);
        Response accountInfo = getAccount(TestConstants.u2_USERNAME, TestConstants.u2_PASSWORD, TestConstants.account_id_2);
        assertEquals(trade_balance_2, accountInfo.jsonPath().getDouble("balance"));

        // update data for user1
        boughtQuantity_1 -= boughtQuantity_2;
        trade_balance_1 += amount; 
        accountInfo = getAccount(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, TestConstants.account_id_1);
        assertEquals(trade_balance_1, accountInfo.jsonPath().getDouble("balance"));
    }
    
    /**
     * Test short selling - return 400 (bad request)
     */ 
    @Test
    @Order(8)
    public void testShort() throws Exception{
        // create open sell trade
        String requestBody = createTradeRequestBody("sell", stockToShort, sellQuantity, 
                0, 0, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        assertEquals(400, tradeInfo.getStatusCode());
    }

    /**
     * Test contra (insufficient fund) - return 400 (bad request)
     */ 
    @Test
    @Order(9)
    public void testContraLimit() throws Exception{
        // create open sell trade
        String requestBody = createTradeRequestBody("buy", stockToContra, 1000000, 
                20, 0, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        assertEquals(400, tradeInfo.getStatusCode());
    }

    @Test
    @Order(10)
    public void testContraMarketOrder() throws Exception{
        // create open sell trade
        String requestBody = createTradeRequestBody("buy", stockToContra, 1000000, 
              0, 0, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        assertEquals(400, tradeInfo.getStatusCode());
    }

    @Test
    @Order(11)
    public void test2ndBuy_Success() throws Exception{
        Response stockInfo = getStockInfo(stockToBuy);
        double ask_price = stockInfo.jsonPath().getDouble("ask");
        int ask_volume = stockInfo.jsonPath().getInt("ask_volume");

        String requestBody = createTradeRequestBody("buy", stockToBuy, minQuantity, 
                0, 0, TestConstants.account_id_1, TestConstants.user_id_1, "");
        Response tradeInfo = createTrade(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, requestBody);
        assertTrade(tradeInfo, 201, "filled", ask_price, minQuantity, stockToBuy);

        // update global variables
        trade_balance_1 -= ask_price * minQuantity;
        
        // check balance of account 1
        Response accountInfo = getAccount(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, TestConstants.account_id_1);
        assertEquals(trade_balance_1, accountInfo.jsonPath().getDouble("balance"));

        // check updated last_price, volume, etc. in stock info
        stockInfo = getStockInfo(stockToBuy);
        assertEquals(ask_price, stockInfo.jsonPath().getDouble("last_price"));
        assertEquals(ask_volume - minQuantity, stockInfo.jsonPath().getInt("ask_volume"));
    }

    /**
     * Test portfolio after buying/selling
     * All numbers are in double, excep quantity (int) or id (int).
        For ROLE_USER only.
        {
                "customer_id": 123456,
                "assests": [
                {
                    "code":"A17U",
                    "quantity":1000,
                    "avg_price": 3.30,
                    "current_price":3.31,
                    "value":3310.0,
                    "gain_loss":10.0
                },
                {
                    "code":"Z74",
                    "avg_price": 2.30,
                    "quantity":2000,
                    "current_price":2.27,
                    "value":4540.0,
                    "gain_loss":-60.0
                }
            ],
            "unrealized_gain_loss":-50.0 (for stocks currently owned),
            "total_gain_loss":500.0 (for all the trades made so far)
     * 
     * @throws Exception
     */
    @Test
    @Order(12)
    public void testPortfolio_Quantities() throws Exception{
        Response res = getPortfolio(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD);
        
        List<String> stocks = res.jsonPath().getList("assets.code");
        List<Integer> quantities = res.jsonPath().getList("assets.quantity");

        assertEquals(stockToTrade, stocks.get(0));
        assertEquals(boughtQuantity_1, quantities.get(0));

        assertEquals(stockToBuy, stocks.get(1));
        assertEquals(minQuantity, quantities.get(1));
        
    }
    /**
     * Test profit/loss calculation.
     * @throws Exception
     */
    @Test
    @Order(13)
    public void testPortfolio_ProfitLoss() throws Exception{
        Response res = getPortfolio(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD);
        
        List<Double> values = res.jsonPath().getList("assets.value");
        double total_gain_loss = res.jsonPath().getDouble("total_gain_loss");
        double total_asset_value = values.stream().mapToDouble(Double::doubleValue).sum();

        Response accountInfo = getAccount(TestConstants.u1_USERNAME, TestConstants.u1_PASSWORD, TestConstants.account_id_1);
        double balance = accountInfo.jsonPath().getDouble("balance");
        assertEquals(trade_balance_1, balance, 0.01);

        // compute the gain_loss and assert
        double expected = (balance + total_asset_value) - TestConstants.account_balance_1;
        assertEquals(expected, total_gain_loss, 0.01);
    }
}