package ryver.app.asset;

import ryver.app.portfolio.*;

import java.math.BigDecimal;

import javax.persistence.*;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Asset{
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    private String code; //symbol of stock eg. A17U
    private long quantity;
    private BigDecimal avg_price; 
    private BigDecimal current_price; 
    private long value; 
    private BigDecimal gain_loss;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    public Asset(String code, long quantity, BigDecimal avg_price, BigDecimal current_price, long value, BigDecimal gain_loss, Portfolio portfolio) {
        this.code = code;
        this.quantity = quantity;
        this.avg_price = avg_price;
        this.current_price = current_price;
        this.value = value;
        this.gain_loss = gain_loss;
        this.portfolio = portfolio;
    }
}