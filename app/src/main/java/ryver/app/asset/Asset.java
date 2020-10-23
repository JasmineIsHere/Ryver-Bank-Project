package ryver.app.asset;

import java.math.BigDecimal;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ryver.app.portfolio.*;

import lombok.*;

@Entity
@Getter
@Setter
// @ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Asset{
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    private String code; //symbol of stock eg. A17U
    private int quantity;
    private BigDecimal avg_price; 
    private BigDecimal current_price; 
    private double value; 
    private BigDecimal gain_loss;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    public Asset(String code, int quantity, BigDecimal avg_price, BigDecimal current_price, double value, BigDecimal gain_loss){
        this.code = code;
        this.quantity = quantity;
        this.avg_price = avg_price;
        this.current_price = current_price;
        this.value = value;
        this.gain_loss = gain_loss;
        // this.portfolio = portfolio;
    }

    @Override public String toString() {
        String string = "Asset(assetid=" + this.id + ", code=" + this.code + ", quantity=" + this.quantity +  ", avg_price=" + this.avg_price +  ", current_price=" + this.current_price +  ", value=" + this.value +  ", gain_loss=" + this.gain_loss +")";
        return string;
    }
}