// package ryver.app.portfolio;

// import java.util.List;

// import ryver.app.customer.*;
// import ryver.app.trade.*;

// import com.fasterxml.jackson.annotation.JsonIgnore;
// import javax.persistence.*;
// import javax.validation.constraints.*;

// import lombok.*;

// @Entity
// @Getter
// @Setter
// @ToString
// @AllArgsConstructor
// @NoArgsConstructor
// @EqualsAndHashCode
// public class Portfolio {
//     private @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
//     private long customer_id;
//     private List<Trade> assets;
//     private double unrealized_gain_loss; // for stocks currently owned
//     private double total_gain_loss;     // for all the trades made so far

//     public Portfolio(Long customer_id) {
//         this.customer_id = customer_id;
//     }
// }