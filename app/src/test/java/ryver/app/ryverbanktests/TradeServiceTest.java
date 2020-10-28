package ryver.app.ryverbanktests;

import ryver.app.customer.*;
import ryver.app.account.*;
import ryver.app.trade.*;
import ryver.app.stock.*;
import ryver.app.customer.Customer.*;
import ryver.app.account.Account.*;
import ryver.app.trade.Trade.*;
import ryver.app.stock.CustomStock.*;

import java.sql.*;

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
 *  W   1.getSpecificStockOpenAndPartialFilledBuyTrade_Found_ReturnListOfTrades
 *  W   2.getSpecificStockOpenAndPartialFilledSellTrade_Found_ReturnListOfTrades 
*/

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {
    @Mock
    BCryptPasswordEncoder encoder;
    @Mock
    private CustomerRepository customers;
    @Mock
    private AccountRepository accounts;
    @Mock
    private StockRepository stocks;

    @Mock
    private TradeRepository trades;
    @InjectMocks
    private TradeController tradeController;

    @Test
    void getSpecificStockOpenAndPartialFilledBuyTrade_Found_ReturnListOfTrades(){
        //arrange
        Customer customer = new Customer(
            "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
        customer.setId(1L);
        
        Account account = new Account(40000.0, 40000.0, 1L, customer);
        
        CustomStock stock = new CustomStock(
            "V03", 20.97, 20000, 20.59, 20000, 20.6, null);

        long timestamp = new Timestamp(System.currentTimeMillis()).getTime();

        Trade trade1 = new Trade("sell", stock.getSymbol(), 400, 2.0, 1.5, timestamp, "open", account.getId(), customer.getId());
        Trade trade2 = new Trade("sell", stock.getSymbol(), 400, 2.0, 1.5, timestamp, "partial-filled", account.getId(), customer.getId());

        ArrayList<Trade> mockListOfTrades = new ArrayList<Trade>();
        mockListOfTrades.add(trade1);
        mockListOfTrades.add(trade2);
    
        when(trades.findByActionAndStatusAndSymbol("buy", "open", stock.getSymbol())).thenReturn(mockListOfTrades);
        when(trades.findByActionAndStatusAndSymbol("buy", "partial-filled", stock.getSymbol())).thenReturn(mockListOfTrades);

        //act
        List<Trade> listOfTrades = tradeController.getSpecificStockOpenAndPartialFilledBuyTrade(stock.getSymbol());

        //assert
        assertNotNull(listOfTrades);
        verify(trades).findByActionAndStatusAndSymbol("buy", "open", stock.getSymbol());
        verify(trades).findByActionAndStatusAndSymbol("buy", "partial-filled", stock.getSymbol());
    }

    @Test
    void getSpecificStockOpenAndPartialFilledSellTrade_Found_ReturnListOfTrades(){
        //arrange
        Customer customer = new Customer(
            "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
        customer.setId(1L);
        
        Account account = new Account(40000.0, 40000.0, 1L, customer);
        
        CustomStock stock = new CustomStock(
            "V03", 20.97, 20000, 20.59, 20000, 20.6, null);

        long timestamp = new Timestamp(System.currentTimeMillis()).getTime();

        Trade trade1 = new Trade("buy", stock.getSymbol(), 400, 2.0, 1.5, timestamp, "open", account.getId(), customer.getId());
        Trade trade2 = new Trade("buy", stock.getSymbol(), 400, 2.0, 1.5, timestamp, "partial-filled", account.getId(), customer.getId());

        ArrayList<Trade> mockListOfTrades = new ArrayList<Trade>();
        mockListOfTrades.add(trade1);
        mockListOfTrades.add(trade2);
    
        when(trades.findByActionAndStatusAndSymbol("sell", "open", stock.getSymbol())).thenReturn(mockListOfTrades);
        when(trades.findByActionAndStatusAndSymbol("sell", "partial-filled", stock.getSymbol())).thenReturn(mockListOfTrades);

        //act
        List<Trade> listOfTrades = tradeController.getSpecificStockOpenAndPartialFilledSellTrade(stock.getSymbol());

        //assert
        assertNotNull(listOfTrades);
        verify(trades).findByActionAndStatusAndSymbol("sell", "open", stock.getSymbol());
        verify(trades).findByActionAndStatusAndSymbol("sell", "partial-filled", stock.getSymbol());
    }
}