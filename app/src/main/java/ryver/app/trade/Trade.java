package ryver.app.trade;

import ryver.app.customer.*;
import ryver.app.account.*;
import ryver.app.stock.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Trade {
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;

    @NotNull(message = "Action should not be null")
    private String action; //buy or sell

    @NotNull(message = "Symbol should not be null")
    private String symbol; //symbol of stock - stock.getSymbol()


    @NotNull(message = "Symbol should not be null. ")
    private int quantity; //in multiples of 100

    private double bid; //for buying
    private double ask; //for selling
    
    private double avg_price; // the average filled price, as one trade can be matched by several other trades
    private int filled_quanity; //# of quantity successfully sold/bought out of total "quantity"

    private long date; //Unix timestamp 

    // @NotNull(message = "Status should not be null. ")
    private String status; //open/filled/partial-filled/cancelled/expired

    @NotNull(message = "Account ID should not be null. ")
    @Column(name = "account_id")
    private Long accountId; //account used to submit buy/sell request

    @NotNull(message = "Customer ID should not be null. ")
    @Column(name = "customer_id")
    private Long customerId;//customer submitting the buy/sell request

    @ManyToOne
    @JoinColumn(name = "account", nullable = false)
    @JsonIgnore
    private Account account;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    @JsonIgnore
    private CustomStock stock;

    public Trade(String action, String symbol, int quantity, double bid, double ask, Long accountId, Long customerId){
        this.action = action;
        this.symbol = symbol;
        this.quantity = quantity;
        this.bid = bid;
        this.ask = ask;
        this.accountId = accountId;
        this.customerId = customerId;
    }

    // for AppApplication
    public Trade(String action, String symbol, int quantity, double bid, double ask, String status, Long accountId, Long customerId, Account account, CustomStock stock){
        this.action = action;
        this.symbol = symbol;
        this.quantity = quantity;
        this.bid = bid;
        this.ask = ask;
        this.status = status;
        this.accountId = accountId;
        this.customerId = customerId;
        this.account = account;
        this.stock = stock;
    }
}