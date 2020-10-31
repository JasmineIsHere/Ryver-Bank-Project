package ryver.app.asset;

import ryver.app.portfolio.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ryver.app.util.jsonDoubleSerializer;

import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Asset{
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    private String code; //symbol of stock eg. A17U
    private int quantity;
    
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double avg_price; 
    
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double current_price;
    
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double value; 
    
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double gain_loss;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    public Asset(String code, int quantity, double avg_price, double current_price, double value, double gain_loss) {
        this.code = code;
        this.quantity = quantity;
        this.avg_price = avg_price;
        this.current_price = current_price;
        this.value = value;
        this.gain_loss = gain_loss;
    }

    public Asset(String code, int quantity, double avg_price, double current_price, double value, double gain_loss, Portfolio portfolio) {
        this.code = code;
        this.quantity = quantity;
        this.avg_price = avg_price;
        this.current_price = current_price;
        this.value = value;
        this.gain_loss = gain_loss;
        this.portfolio = portfolio;
    }

    @Override public String toString() {
        String string = "Asset(assetid=" + this.id + ", code=" + this.code + ", quantity=" + this.quantity +  ", avg_price=" + this.avg_price +  ", current_price=" + this.current_price +  ", value=" + this.value +  ", gain_loss=" + this.gain_loss +")";
        return string;
    }
}