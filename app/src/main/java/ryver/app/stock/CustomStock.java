package ryver.app.stock;

import ryver.app.trade.*;

import java.math.BigDecimal;
import java.util.*;
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
public class CustomStock {

    private @Id String symbol; //symbol of stock eg. A17U
    private BigDecimal last_price; //$$
    private long bid_volume; //qty
    private BigDecimal bid; //$$
    private long ask_volume; //qty
    private BigDecimal ask; //$$

    // @OneToMany(mappedBy = "stock",
    // orphanRemoval = true,
    // cascade = CascadeType.ALL)
    // private List<Trade> trades;
}
