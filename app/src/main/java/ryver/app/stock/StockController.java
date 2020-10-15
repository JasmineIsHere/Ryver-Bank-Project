package ryver.app.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.Stock;

@RestController
public class StockController {
    private StockRepository stocks;
   
    public StockController(StockRepository stocks){
        this.stocks = stocks;
    }

    public ArrayList<CustomStock> initiateStocks() {
        // true as of 12/10/2020
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
                stock.setSymbol(quote.getSymbol());
                stock.setAsk(quote.getAsk());
                stock.setBid(quote.getBid());
                stock.setLast_price(quote.getPreviousClose());
                stock.setAsk_volume(20000); // set as 20000 volume
                stock.setBid_volume(20000); // set as 20000 volume
                stockList.add(stock);
            }
            
            return stockList;
        } catch(IOException ex){
            System.out.println("Error retrieving data");
        }

        return null;
    }  

    @GetMapping("/stocks")
    public List<CustomStock> getStocks() {
        // true as of 12/10/2020
        return stocks.findAll();
    }  

    @GetMapping("/stocks/{symbol}")
    public CustomStock getStockBySymbol(@PathVariable (value = "symbol") String symbol){
        return stocks.findBySymbol(symbol.toUpperCase());
    }

    //@GetMapping("/stocks/{symbol}")
    // public CustomStock getSpecificStock(@PathVariable (value = "symbol") String symbol){
    //     CustomStock stock = new CustomStock();

    //     try {
    //         Stock s = YahooFinance.get(symbol);
    //         StockQuote quote = s.getQuote();
    //         stock.setSymbol(quote.getSymbol());
    //         stock.setAsk(quote.getAsk());
    //         stock.setBid(quote.getBid());
    //         stock.setAsk_volume(quote.getAskSize());
    //         stock.setBid_volume(quote.getBidSize());
    //         stock.setLast_price(quote.getPreviousClose());
    //     } catch(IOException ex){
    //         System.out.println("Error retrieving data");
    //     }

    //     return stock;
    // }
}