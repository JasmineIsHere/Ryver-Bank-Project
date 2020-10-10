package ryver.app.stock;

import ryver.app.trade.*;

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
public class Stock {

    private @Id String symbol; //symbol of stock eg. A17U
    private double last_price; //$$
    private int bid_volume; //qty
    private double bid; //$$
    private int ask_volume; //qty
    private double ask; //$$

    @OneToMany(mappedBy = "stock",
    orphanRemoval = true,
    cascade = CascadeType.ALL)
    private List<Trade> trades;
}
