// package statement of Account;
package ryver.app.account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

public interface AccountRepository extends JpaRepository <Account, Long> {
    List<Account> findByUserId(Long UID);
    Optional<Account> findByIdAndUserId(Long id, Long UID);
}
