package ryver.app.trade;

import ryver.app.account.*;
import ryver.app.stock.*;

import javax.persistence.*;
import javax.validation.constraints.*;


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

    private String action; //buy or sell
    private String symbol; //symbol of stock - stock.getSymbol()
    private int quantity; //in multiples of 100
    private double bid; //for buying
    private double ask; //for selling
    private double avg_price; 
    private int filled_quanity; //# of quantity successfully sold/bought out of total "quantity"
    private long timestamp; //Unix timestamp //Timestamp object somewhere //Timestamp.getTime()
    private String status; //open/filled/partial-filled/cancelled/expired
    private Long account_id; //account used to submit buy/sell request
    private Long customer_id;//customer submitting the buy/sell request

    @ManyToOne
    @JoinColumn(name = "account", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;
}