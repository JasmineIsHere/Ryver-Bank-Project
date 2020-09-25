// Package statement here
package ryver.app.account;
import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ryver.app.user.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Account {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long AID;
    
    
    private double balance;
    private double availBalance;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Account(double balance) {
        System.out.println(AID);
        this.balance = balance;
        this.availBalance = balance;
    }
    
    // public Account getAccount() {
    //     return AID
    // }
}