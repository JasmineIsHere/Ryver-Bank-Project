package ryver.app.stock;

import ryver.app.trade.*;

import java.math.BigDecimal;
import java.util.*;
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
public class CustomStock {
    // only keeps the best open trade
    // if the trade gets filled then revert back to the next best priced open trade
    // best price - lowest ask, highest bid. if same price -> get the earlier submitted trade

    private @Id String symbol; //symbol of stock eg. A17U
    private BigDecimal last_price; //$$
    private long bid_volume; //qty
    private BigDecimal bid; //$$
    private long ask_volume; //qty
    private BigDecimal ask; //$$

    @OneToMany(mappedBy = "stock",
    // orphanRemoval = true,
    cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Trade> trades;

    // public CustomStock(String symbol, BigDecimal last_price, long bid_volume, long ask_volume, BigDecimal ask, List<Trade> trades){
    //     this.symbol = symbol;
    //     this.last_price = last_price;
    //     this.bid_volume = bid_volume;
    //     this.bid = bid;
    //     this.ask_volume = ask_volume;
    //     this.ask = ask;
    //     this.trades = trades;
    // } 
}
