package ryver.app.transaction;

import ryver.app.customer.*;
import ryver.app.account.*;

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
public class Transaction {
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    
    private Long sender; // sender's account id
    private Long receiver; // receiver's account id
    private double amount;

    // // @ManyToMany (fetch = FetchType.EAGER)
    // @ManyToMany
    // // @Fetch (value=FetchMode.SELECT)
    // @JoinTable(name = "account_transactions",
    //     joinColumns = @JoinColumn(name = "transaction_id"),
    //     inverseJoinColumns = @JoinColumn(name = "account_id"))
    // private Set<Account> account;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}