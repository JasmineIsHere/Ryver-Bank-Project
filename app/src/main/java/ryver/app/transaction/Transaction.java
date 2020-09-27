package ryver.app.transaction;

import javax.persistence.*;
import javax.validation.constraints.*;

import ryver.app.customer.*;
import ryver.app.account.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Transaction {
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    
    private Long sender;
    private Long receiver;
    private double amount;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}