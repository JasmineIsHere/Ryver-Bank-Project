package ryver.app.account;

import ryver.app.customer.*;
import ryver.app.trade.*;
import ryver.app.transaction.*;
import ryver.app.util.jsonDoubleSerializer;

import java.util.*;
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

public class Account {
    // Fields
    /**
     * The auto-generated ID for each account Starts from 1
     */
    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    /**
     * The amount of money in the account
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double balance;

    /**
     * If funds are on-hold, money is deducted from here first
     */
    @JsonSerialize(using = jsonDoubleSerializer.class)
    private double available_balance;

    /**
     * The ID of the Customer that the account is associated with
     */
    private long customer_id;

    // Mappings
    /**
     * The Customer object associated with this Account
     */
    @ManyToOne
    @JoinColumn(name = "customer", nullable = false)
    @JsonIgnore
    private Customer customer;

    /**
     * The list of Transactions associated with this Account
     */
    @OneToMany(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Transaction> transactions;

    /**
     * The list of Trades associated with this Account
     */
    @OneToMany(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Trade> trades;

    // Constructors
    /**
     * Create an Account with the specified balance, available_balance and
     * customer_id
     */
    public Account(double balance, double available_balance, long customer_id) {
        this.balance = balance;
        this.available_balance = available_balance;
        this.customer_id = customer_id;
    }

    /**
     * Create an Account with the specified balance, available_balance, customer_id
     * and Customer object Used to create inital users in AppApplication.java and
     * for testing only
     */
    public Account(double balance, double available_balance, long customer_id, Customer customer) {
        this.balance = balance;
        this.available_balance = available_balance;
        this.customer_id = customer_id;
        this.customer = customer;
    }

}