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
    private /*@Id @GeneratedValue (strategy = GenerationType.IDENTITY)*/ Long AID;
    private Long UID;
    private double balance;
    private double availBalance;

    public Account(Long UID, Long AID, double balance) {
        this.UID = UID;
        this.AID = AID;
        this.balance = balance;
        this.availBalance = balance;
    }
    @ManyToOne
    @JoinColumn(name = "UID", nullable = false)
    private User user;
    // public Account getAccount() {
    //     return AID
    // }
}