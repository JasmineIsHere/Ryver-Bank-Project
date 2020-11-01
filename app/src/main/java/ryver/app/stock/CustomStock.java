package ryver.app.stock;

import ryver.app.trade.*;

import java.util.*;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ryver.app.util.jsonDoubleSerializer;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CustomStock {

    private @Id String symbol; //symbol of stock eg. A17U

    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double last_price; 
    

    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double bid; 
    private int bid_volume; 

    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double ask; //$$
    private int ask_volume; 

    @OneToMany(mappedBy = "stock",
    cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Trade> trades;

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
