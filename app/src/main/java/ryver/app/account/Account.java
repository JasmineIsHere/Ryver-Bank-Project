package ryver.app.account;

import javax.persistence.*;
import javax.validation.constraints.*;

import ryver.app.user.*;
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
    
    // null elements are considered valid, so we need a size constraints too
    @NotNull(message = "Account should not be null")
    private double Account;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}