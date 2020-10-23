package ryver.app.asset;

import ryver.app.portfolio.*;

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
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) int id;
    private String code; //symbol of stock eg. A17U
    private int quantity;
    private double avg_price; 
    private double current_price; 
    private double value; 
    private double gain_loss;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    public Asset(String code, int quantity, double avg_price, double current_price, long value, double gain_loss, Portfolio portfolio) {
        this.code = code;
        this.quantity = quantity;
        this.avg_price = avg_price;
        this.current_price = current_price;
        this.value = value;
        this.gain_loss = gain_loss;
        this.portfolio = portfolio;
    }
}