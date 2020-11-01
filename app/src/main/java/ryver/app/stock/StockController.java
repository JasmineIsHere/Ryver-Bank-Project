package ryver.app.stock;

import java.io.IOException;
import java.util.*;

import org.springframework.web.bind.annotation.*;

import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.Stock;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class StockController {
    // Repositories
    private StockRepository stocks;
   
    public StockController(StockRepository stocks){
        this.stocks = stocks;
    }

    /**
     * Get stock data from YahooFinance API
     * and create the corresponding Stock objects
     *  
     * @return ArrayList<CustomStock>
     */
    public ArrayList<CustomStock> initiateStocks() {
        String[] symbols = new String[] {"A17U.SI", "C61U.SI", "C31.SI", "C38U.SI", "C09.SI", "C52.SI", "D01.SI", "D05.SI", "G13.SI",
        "H78.SI", "C07.SI", "J36.SI", "J37.SI", "BN4.SI", "N2IU.SI", "ME8U.SI", "M44U.SI", "O39.SI", "S58.SI", "U96.SI", "S68.SI", "C6L.SI", 
        "Z74.SI", "S63.SI", "Y92.SI", "U11.SI", "U14.SI", "V03.SI", "F34.SI", "BS6.SI"};
        
        try {
            Map<String, Stock> stockMap = YahooFinance.get(symbols,true);
            ArrayList<CustomStock> stockList = new ArrayList<CustomStock>(); 
            
            for(int i=0; i< symbols.length; i++){
                Stock s = stockMap.get(symbols[i]);
                CustomStock stock = new CustomStock();
                StockQuote quote = s.getQuote();

                // To remove the .si from the stock symbol
                String originalSymbol = quote.getSymbol();
                String[] splitSymbol = originalSymbol.split("\\.");
                
                stock.setSymbol(splitSymbol[0]);
                stock.setAsk(quote.getAsk().doubleValue());
                stock.setBid(quote.getBid().doubleValue());
                stock.setLast_price(quote.getPreviousClose().doubleValue());
                stock.setAsk_volume(20000); // Set as 20000 volume
                stock.setBid_volume(20000); // Set as 20000 volume
                stockList.add(stock);
                stocks.save(stock);
            }
            
            return stockList;
        } catch(IOException ex){
            System.out.println("Error retrieving data");
        }

        return null;
    }  

    /**
     * Get the List of CustomStocks to put in the market
     * Returns 200 OK (if no exceptions)
     * 
     * @return List<CustomStock>
     */
    @GetMapping("/api/stocks")
    public List<CustomStock> getStocks() {
        return stocks.findAll();
    }

    /**
     * Get a CustomStock, based on the symbol
     * Returns 200 OK (if no exceptions)
     * 
     * @param symbol
     * @return CustomStock
     */
    @GetMapping("/api/stocks/{symbol}")
    public CustomStock getStockBySymbol(@PathVariable (value = "symbol") String symbol){
        return stocks.findBySymbol(symbol)
            .orElseThrow(() -> new InvalidStockException(symbol));
    }
}