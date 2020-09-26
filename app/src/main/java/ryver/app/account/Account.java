package ryver.app.account;

import javax.persistence.*;
import javax.validation.constraints.*;

import ryver.app.customer.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Account {
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    
    // null elements are considered valid, so we need a size constraints too
    // primitive types can't have null, so it's auto 0.0
    private double balance;
    private double available_balance;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}