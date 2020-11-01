package ryver.app.asset;

import ryver.app.portfolio.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ryver.app.util.jsonDoubleSerializer;

import lombok.*;

// Spring Annotations
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

public class Asset {
    // Fields
    /**
     * The auto-generated ID for each Asset 
     * Starts from 1
     */
    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    /**
     * The symbol of the stock eg. A17U
     * From trade - symbol
     */
    private String code;

    /**
     * The quantity of stocks owned
     * From trade - quantity
     */
    private int quantity;

    /**
     * The average price of the stocks owned
     * From trade - avg price
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double avg_price;

    /**
     * The current price of the stocks owned
     * From trade - bid price
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double current_price;

    /**
     * The value of the stocks owned 
     * value = current_price * quantity
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double value;

    /**
     * The gain/loss of the stocks owned 
     * gain_loss = value - (avg_price * quantity)
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double gain_loss;

    // Mappings
    /**
     * The ID of the associated Portfolio
     */
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    // Constructors
    /**
     * Create an Asset with the specified parameters
     * 
     * @param code
     * @param quantity
     * @param avg_price
     * @param current_price
     * @param value
     * @param gain_loss
     */
    public Asset(String code, int quantity, double avg_price, double current_price, double value, double gain_loss) {
        this.code = code;
        this.quantity = quantity;
        this.avg_price = avg_price;
        this.current_price = current_price;
        this.value = value;
        this.gain_loss = gain_loss;
    }

    /**
     * Create an Asset with the specified parameters (With associated Portfolio)
     * 
     * @param code
     * @param quantity
     * @param avg_price
     * @param current_price
     * @param value
     * @param gain_loss
     * @param portfolio
     */
    public Asset(String code, int quantity, double avg_price, double current_price, double value, double gain_loss,
            Portfolio portfolio) {
        this.code = code;
        this.quantity = quantity;
        this.avg_price = avg_price;
        this.current_price = current_price;
        this.value = value;
        this.gain_loss = gain_loss;
        this.portfolio = portfolio;
    }

    /**
     * Convert Asset to String
     * 
     * @return String
     */
    @Override
    public String toString() {
        String string = "Asset(assetid=" + this.id + ", code=" + this.code + ", quantity=" + this.quantity
                + ", avg_price=" + this.avg_price + ", current_price=" + this.current_price + ", value=" + this.value
                + ", gain_loss=" + this.gain_loss + ")";
        return string;
    }
}