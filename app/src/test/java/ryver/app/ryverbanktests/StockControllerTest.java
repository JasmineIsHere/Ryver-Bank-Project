package ryver.app.ryverbanktests;


import ryver.app.stock.*;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 
 * KEY: 
 * W --> Works 
 * X --> Doesnt work
 * 
 *      T E S T
 *  W   1.getStockBySymbol_Found_returnStocks
 */

@ExtendWith(MockitoExtension.class)
public class StockControllerTest {
    @Mock
    private StockRepository stocks;

    @InjectMocks
    private StockController stockController;

    @Test
    void getStockBySymbol_Found_returnStocks(){
        //arrange
        CustomStock stock = new CustomStock(
            "V03", 20.97, 20000, 20.59, 20000, 20.6, null);
        when(stocks.findBySymbol(stock.getSymbol())).thenReturn(Optional.of(stock));

        //act
        CustomStock getStock = stockController.getStockBySymbol(stock.getSymbol());

        //assert
        assertNotNull(getStock);
        verify(stocks).findBySymbol(stock.getSymbol());
    }
}