package ryver.app.account;

import ryver.app.customer.*;
import ryver.app.transaction.*;
import ryver.app.util.jsonDoubleSerializer;
import ryver.app.trade.*;

import java.util.*;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
    
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double balance;

    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double available_balance;
    private long customer_id;

    @ManyToOne
    @JoinColumn(name = "customer", nullable = false)
    @JsonIgnore
    private Customer customer;

    public Account(double balance, double available_balance, long customer_id) {
        this.balance = balance;
        this.available_balance = available_balance;
        this.customer_id = customer_id;
    }

    // for AppApplication 
    // JOLENE: i think can just leave this here, used it for my testing
    public Account(double balance, double available_balance, long customer_id, Customer customer) {
        this.balance = balance;
        this.available_balance = available_balance;
        this.customer_id = customer_id;
        this.customer = customer;
    }

    @OneToMany(mappedBy = "account",
    orphanRemoval = true,
    cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "account",
    orphanRemoval = true,
    cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Trade> trades;
}