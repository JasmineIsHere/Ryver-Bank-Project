package ryver.app.account;

import ryver.app.customer.*;
import ryver.app.transaction.*;
import ryver.app.trade.*;

import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;

import java.lang.Object;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    
    private double balance;
    private double available_balance;
    private long customer_id;

    @ManyToOne
    @JoinColumn(name = "customer", nullable = false)
    private Customer customer;

    public Account(double balance, double available_balance, long customer_id) {
        this.balance = balance;
        this.available_balance = available_balance;
        this.customer_id = customer_id;
    }

    // for AppApplication
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