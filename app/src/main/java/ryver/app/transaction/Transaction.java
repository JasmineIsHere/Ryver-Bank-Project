package ryver.app.transaction;

import ryver.app.account.*;
import ryver.app.util.jsonDoubleSerializer;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.*;

// Spring Annotations
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

public class Transaction {
    // Fields
    /**
     * The auto-generated ID for each Transaction 
     * Starts from 1
     */
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;

    /**
     * The sender of the Transaction
     * Sender's account id
     */
    @Column(name = "\"from\"")
    private Long from; 

    /**
     * The receiver of the Transaction
     * Receiver's account id
     */
    private Long to;

    /**
     * The amount of the Transaction
     * Amount being transferred
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double amount;

    // Mappings
    /**
     * The Account associated with the Transaction
     */
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    // Constructors
    /**
     * Create a Transaction with the specified parameters
     * 
     * @param from
     * @param to
     * @param amount
     * @param account
     */
    public Transaction(long from, long to, double amount, Account account){
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.account = account;
    }
}