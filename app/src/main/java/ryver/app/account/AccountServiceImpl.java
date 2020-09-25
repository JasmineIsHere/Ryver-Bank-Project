package ryver.app.account;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

import ryver.app.user.*;

@Service
public class AccountServiceImpl implements AccountService {
   
    private AccountRepository accounts; //AccountRepository.java yet to be created
    private UserController users;

    public AccountServiceImpl(AccountRepository accounts,UserController users){ //injection
        this.accounts = accounts;
        this.users = users;
    }

    @Override
    public List<Account> listAccounts(Long UID) { //Account.java yet to be created
        return accounts.findAll();
    }

    
    @Override
    public Account getAccount(Long AID){ //Account.java yet to be created
        
        return accounts.findById(AID).map(account -> {
            return account;
        }).orElse(null);

    }
    
    public Account addAccount(long userId, long accountId, double balance, double availBalance){
        Account account = new Account();
        account.setAID(accoundId);
        account.setBalance(balance);
        account.setAvailBalance(availBalance);
        return users.getUser(userId).map(user ->{
            account.setUser(user);
            return accounts.save(account);
        }).orElseThrow(() -> new ryver.app.account.AccountNotFoundException(accountId));
    }
}