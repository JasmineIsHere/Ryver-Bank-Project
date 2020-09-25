package ryver.app.account;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import ryver.app.user.UserRepository;
import ryver.app.user.UserNotFoundException;

@RestController
public class AccountController {
    private AccountRepository accounts;
    private UserRepository users;

    public AccountController(AccountRepository accounts, UserRepository users){
        this.accounts = accounts;
        this.users = users;
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @GetMapping("/users/{userId}/accounts")
    public List<Account> getAllAccountsByUserId(@PathVariable (value = "userId") Long userId) {
        if(!users.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return accounts.findByUserId(userId);
    }

    @GetMapping("/users/{userId}/accounts/{accountId}")
    public Account getAccountByAccountIdAndUserId(@PathVariable (value = "accountId") Long accountId, 
        @PathVariable (value = "userId") Long userId) {
        
        if(!users.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return accounts.findByIdAndUserId(accountId, userId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @PostMapping("/users/{userId}/accounts")
    public Account addAccount (@PathVariable (value = "userId") Long userId, @Valid @RequestBody Account account) {
        return users.findById(userId).map(user ->{
            account.setUser(user);
            return accounts.save(account);
        }).orElseThrow(() -> new UserNotFoundException(userId));
    }

}