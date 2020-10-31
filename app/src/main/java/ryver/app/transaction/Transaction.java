package ryver.app.transaction;

import ryver.app.account.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ryver.app.util.jsonDoubleSerializer;

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

    @Column(name = "\"from\"")
    private Long from; // sender's account id
    private Long to; // receiver's account id

    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double amount;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    public Transaction(long from, long to, double amount, Account account){
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.account = account;
    }
}