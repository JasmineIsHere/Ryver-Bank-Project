package ryver.app.portfolio;

import ryver.app.customer.*;
import ryver.app.asset.Asset;
import ryver.app.util.jsonDoubleSerializer;

import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Portfolio {
    private @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    
    private long customer_id;

    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double unrealized_gain_loss; // for stocks currently owned

    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double total_gain_loss;     // for all the trades made so far

    @OneToOne
    @JoinColumn(name = "customer", nullable = false)
    @JsonIgnore
    private Customer customer;

    @OneToMany(mappedBy = "portfolio",
    orphanRemoval = true,
    cascade = CascadeType.ALL)
    private List<Asset> assets;

    public Portfolio(long customer_id, double unrealized_gain_loss, double total_gain_loss, Customer customer, List<Asset> assets){
        this.customer_id = customer_id;
        this.unrealized_gain_loss = unrealized_gain_loss;
        this.total_gain_loss = total_gain_loss;
        this.customer =customer;
        this.assets = assets;
    }
}


/*NOTE: 
Asset
                    "code":"A17U",           From trade - symbol
                    "quantity":1000,         From trade - quantity
                    "avg_price": 3.30,       From trade - avg price
                    "current_price":3.31,    From trade - bid price???
                    "value":3310.0,          current_price * quantity
                    "gain_loss":10.0         value - (avg_price * quantity)

Mapping
Customer - Portfolio: OnetoOne
Portfolio - Asset(Trade?): OnetoMany
Asset - Portfolio/Customer: ManytoOne
*/