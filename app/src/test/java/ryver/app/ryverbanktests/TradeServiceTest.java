// package test.java.ryver.app;

// import ryver.app.customer.*;
// import ryver.app.account.*;
// import ryver.app.transaction.*;
// import ryver.app.trade.*;
// import ryver.app.stock.*;
// import ryver.app.customer.Customer.*;
// import ryver.app.account.Account.*;
// import ryver.app.transaction.Transaction.*;
// import ryver.app.trade.Trade.*;
// import ryver.app.stock.Stock.*;

// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.ApplicationContext;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// /** 
//  * KEY: 
//  * W --> Works 
//  * X --> Doesnt work
//  * 
//  *      T E S T
//  *  X   1.getSpecificStockOpenAndPartialFilledBuyTrade_Found_ReturnListOfTrades
//  *  X   2.getSpecificStockOpenAndPartialFilledSellTrade_Found_ReturnListOfTrades 
//  *  String action, 
//  * String symbol,
//  *  int quantity,
//  *  double bid,
//  *  double ask,
//  *  String status,
//  *  Long accountId,
//  *  Long customerId){
//  * Notes:
//  * none
// */

// @ExtendWith(MockitoExtension.class)
// public class TradeServiceTest {
//     @Mock
//     BCryptPasswordEncoder encoder;
//     @Mock
//     private CustomerRepository customers;
//     @Mock
//     private AccountRepository accounts;
//     @Mock
//     private StockRepository stocks;

//     @Mock
//     private TradeRepository trades;
//     @InjectMocks
//     private TradeController tradeController;


//     // Trade trade1 = new Trade("sell", symbol, (int)stock.getAsk_volume(), 0.0, stock.getAsk().doubleValue(), "open", 1L, 3L);

//     // Trade trade2 = new Trade("buy", symbol, (int)stock.getBid_volume(), stock.getBid().doubleValue(), 0.0, "open", 1L, 3L);

//     @Test
//     void getSpecificStockOpenAndPartialFilledBuyTrade_Found_ReturnListOfTrades(){
//         //arrange
//         Customer customer = new Customer(
//             "good_user_1", "01_user_01", "ROLE_USER", "User One", "S7812345A", "91234567", "123 Ang Mo Kio Road S456123", true);
//         customer.setId(1L);
//         Account account = new Account(400.0, 400.0, 1L, customer);
//         CustomStock stock = new CustomStock(
//              "V03", "20.97", "20000", "20.59", "20000", "20.6", "null");
//         Trade trade = new Trade(
//             "sell", stock.getSymbol(), (int)stock.getAsk_volume(), 0.0, stock.getAsk().doubleValue(), "open", customer.getId(), account.getId());

//         when(trades.findByActionAndStatusAndSymbol("sell", "open", trade.symbol())).thenReturn(trade);
//         when(trades.findByActionAndStatusAndSymbol("sell", "partial-filled", trade.symbol())).thenReturn(trade);

//         //act
//         List<Trade> listOfTrades = tradeController.getSpecificStockOpenAndPartialFilledBuyTrade(trade.getSymbol());

//         //assert
//         assertNotNull(listOfTrades);
//         verify(trades).findByActionAndStatusAndSymbol("sell", "open", trade.symbol());
//         verify(trades).findByActionAndStatusAndSymbol("sell", "partial-filled", trade.symbol());
//     }

//     // @Test
//     // void addTransaction_newTransaction_returnSavedTransaction(){
//     //     //arrange
        
//     //     //act

//     //     //assert
//     // }
// }