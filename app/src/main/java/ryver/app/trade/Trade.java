package ryver.app.trade;

import ryver.app.portfolio.Portfolio;
import ryver.app.account.*;
import ryver.app.stock.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Entity
@Getter
@Setter
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
    private int filled_quantity; //# of quantity successfully sold/bought out of total "quantity"

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

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore
    private Portfolio portfolio;

    public Trade(String action, String symbol, int quantity, double bid, double ask, Long accountId, Long customerId){
        this.action = action;
        this.symbol = symbol;
        this.quantity = quantity;
        this.bid = bid;
        this.ask = ask;
        this.accountId = accountId;
        this.customerId = customerId;
    }

    // for AppApplication (market maker trades and 20k initial stocks)
    public Trade(String action, String symbol, int quantity, double bid, double ask, long date, String status, Long accountId, Long customerId){
        this.action = action;
        this.symbol = symbol;
        this.quantity = quantity;
        this.bid = bid;
        this.ask = ask;
        this.date = date;
        this.status = status;
        this.accountId = accountId;
        this.customerId = customerId;
    }

    @Override public String toString() {
        String string = "Trade(tradeid=" + this.id + ", action=" + this.action + ", symbol=" + this.symbol +  ", quantity=" + this.quantity +  ", bid=" + this.bid +  ", ask=" + this.ask +  ", avg_price=" + this.avg_price +  ", filled_quantity=" + this.filled_quantity +  ", date=" + this.date +  ", account_id=" + this.accountId +  ", customer_id=" + this.customerId +  ", status=" + this.status + ")";
        return string;
    }
}