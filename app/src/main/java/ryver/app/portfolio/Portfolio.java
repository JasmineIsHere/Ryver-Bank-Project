package ryver.app.portfolio;

import ryver.app.customer.*;
import ryver.app.asset.*;
import ryver.app.util.jsonDoubleSerializer;

import java.util.List;
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

public class Portfolio {
    //Fields
    /**
     * The auto-generated ID for each Portfolio 
     * Starts from 1
     */
    private @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    
    /**
     * The ID of the associated Customer
     */
    private long customer_id;

    /**
     * The unrealized gain/loss of all the Assets in the Portfolio
     * For currently owned Stocks
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double unrealized_gain_loss; 

    /**
     * The total gain/loss of all the Assets in the Portfolio
     * For all the Trades made so far
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double total_gain_loss; 

    // Mappings
    /**
     * The Customer associated with the Portfolio
     */
    @OneToOne
    @JoinColumn(name = "customer", nullable = false)
    @JsonIgnore
    private Customer customer;

    /**
     * The List of Assets associated with the Portfolio
     */
    @OneToMany(mappedBy = "portfolio",
    orphanRemoval = true,
    cascade = CascadeType.ALL)
    private List<Asset> assets;

    // Constructors
    /**
     * Create a Portfolio with the specified parameters
     * 
     * @param customer_id
     * @param unrealized_gain_loss
     * @param total_gain_loss
     * @param customer
     * @param assets
     */
    public Portfolio(long customer_id, double unrealized_gain_loss, double total_gain_loss, Customer customer, List<Asset> assets){
        this.customer_id = customer_id;
        this.unrealized_gain_loss = unrealized_gain_loss;
        this.total_gain_loss = total_gain_loss;
        this.customer = customer;
        this.assets = assets;
    }
}