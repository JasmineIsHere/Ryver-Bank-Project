package ryver.app.trade;

import ryver.app.portfolio.*;
import ryver.app.account.*;
import ryver.app.stock.*;
import ryver.app.util.jsonDoubleSerializer;

import javax.persistence.*;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Trade {
    // Fields
    /**
     * The auto-generated ID for each Trade 
     * Starts from 1
     */
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;

    /**
     * The action of the Trade (Buy or Sell)
     * The action must not be null
     */
    @NotNull(message = "Action should not be null")
    private String action;

    /**
     * The symbol of the CustomStock being traded
     * The symbol must not be null
     * From CustomStock - stock.getSymbol()
     */
    @NotNull(message = "Symbol should not be null")
    private String symbol; // 

    /**
     * The quantity of the CustomStock being traded
     * The quantity must not be null
     * In multiples of 100
     */
    @NotNull(message = "Quantity should not be null. ")
    private int quantity;

    /**
     * The bid of the CustomStock being traded
     * For buying
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double bid; //for buying

    /**
     * The ask of the CustomStock being traded
     * For selling
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double ask;

    /**
     * The average filled price of the CustomStock being traded
     * One trade can be matched by several other trades
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double avg_price;

    /**
     * The filled quantity of the CustomStock being traded
     * Quantity successfully sold/bought out of total "quantity"
     */
    private int filled_quantity;

    /**
     * The date and time of the Trade
     * Unix timestamp
     */
    private long date;

    /**
     * The status of the Trade
     * open/filled/partial-filled/cancelled/expired
     */
    private String status;

    // Mappings
    /**
     * The ID of the Account associated with the Trade
     * Account used to submit buy/sell request
     */
    @NotNull(message = "Account ID should not be null. ")
    @Column(name = "account_id")
    @JsonProperty("account_id")
    private Long accountId;

    /**
     * The ID of the Customer associated with the Trade
     * Customer submitting the buy/sell request
     */
    @NotNull(message = "Customer ID should not be null. ")
    @Column(name = "customer_id")
    @JsonProperty("customer_id")
    private Long customerId;

    /**
     * The Account associated with the Trade
     */
    @ManyToOne
    @JoinColumn(name = "account", nullable = false)
    @JsonIgnore
    private Account account;

    /**
     * The ID of the CustomStock associated with the Trade
     */
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    @JsonIgnore
    private CustomStock stock;

    /**
     * The ID of the Portfolio associated with the Trade
     * Based on Customer making the Trade
     */
    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore
    private Portfolio portfolio;

    // Constructors
    /**
     * Create a Trade with the specified parameters
     * 
     * @param action
     * @param symbol
     * @param quantity
     * @param bid
     * @param ask
     * @param accountId
     * @param customerId
     */
    public Trade(String action, String symbol, int quantity, double bid, double ask, Long accountId, Long customerId){
        this.action = action;
        this.symbol = symbol;
        this.quantity = quantity;
        this.bid = bid;
        this.ask = ask;
        this.accountId = accountId;
        this.customerId = customerId;
    }

    /**
     * Create a Trade with the specified parameters
     * For AppApplication (Market maker trades and 20k initial stocks)
     * 
     * @param action
     * @param symbol
     * @param quantity
     * @param bid
     * @param ask
     * @param date
     * @param status
     * @param accountId
     * @param customerId
     */
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

    /**
     * Convert Trade to String
     * 
     * @return String
     */
    @Override public String toString() {
        String string = "Trade(tradeid=" + this.id + ", action=" + this.action + ", symbol=" + this.symbol +  ", quantity=" + this.quantity +  ", bid=" + this.bid +  ", ask=" + this.ask +  ", avg_price=" + this.avg_price +  ", filled_quantity=" + this.filled_quantity +  ", date=" + this.date +  ", account_id=" + this.accountId +  ", customer_id=" + this.customerId +  ", status=" + this.status + ")";
        return string;
    }
}