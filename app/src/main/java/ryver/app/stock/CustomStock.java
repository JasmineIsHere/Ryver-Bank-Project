package ryver.app.stock;

import ryver.app.trade.*;
import ryver.app.util.jsonDoubleSerializer;

import java.util.*;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.*;

// Spring Annotations
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

public class CustomStock {
    // Fields
    /**
     * The symbol of the CustomStock 
     * eg. A17U
     */
    private @Id String symbol; //

    /**
     * The last price of the CustomStock
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double last_price; 
    
    /**
     * The bid of the CustomStock
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double bid; 

    /**
     * The bid volume of the CustomStock
     */
    private int bid_volume; 

    /**
     * The ask price of the CustomStock
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double ask;

    /**
     * The ask volume of the CustomStock
     */
    private int ask_volume; 

    // Mappings
    /**
     * The List of Trades associated with the CustomStock
     */
    @OneToMany(mappedBy = "stock",
    cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Trade> trades;

    // Constructors
    /**
     * Create a CustomStock with the specified parameters
     * 
     * @param symbol
     * @param last_price
     * @param bid_volume
     * @param bid
     * @param ask_volume
     * @param ask
     * @param trades
     */
    public CustomStock(String symbol, double last_price, int bid_volume, double bid,int ask_volume, double ask, List<Trade> trades){
        this.symbol = symbol;
        this.last_price = last_price;
        this.bid_volume = bid_volume;
        this.bid = bid;
        this.ask_volume = ask_volume;
        this.ask = ask;
        this.trades = trades;
    } 
}
