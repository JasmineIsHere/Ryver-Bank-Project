package ryver.app.transaction;

import ryver.app.account.*;
import ryver.app.util.jsonDoubleSerializer;

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
}