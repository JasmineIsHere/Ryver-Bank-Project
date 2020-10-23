package ryver.app.ryverbanktests;

import ryver.app.customer.*;
import ryver.app.account.*;
import ryver.app.stock.*;
import ryver.app.customer.Customer.*;
import ryver.app.account.Account.*;
import ryver.app.stock.CustomStock.*;
import java.math.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
 * 
 * Notes:
 * none
*/

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {
    @Mock
    private StockRepository stocks;

    @InjectMocks
    private StockController stockController;

    @Test
    void getStockBySymbol_Found_returnStocks(){
        //arrange
        CustomStock stock = new CustomStock(
            "V03", BigDecimal.valueOf(20.97), 20000, BigDecimal.valueOf(20.59), 20000, BigDecimal.valueOf(20.6), null);
        when(stocks.findBySymbol(stock.getSymbol())).thenReturn(Optional.of(stock));

        //act
        CustomStock getStock = stockController.getStockBySymbol(stock.getSymbol());

        //assert
        assertNotNull(getStock);
        verify(stocks).findBySymbol(stock.getSymbol());
    }
}