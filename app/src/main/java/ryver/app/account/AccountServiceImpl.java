//package csd.sprint1.account; //to be updated based on directory
package ryver.app.account;
import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class AccountServiceImpl implements AccountService {
   
    private AccountRepository accounts; //AccountRepository.java yet to be created
    

    public AccountServiceImpl(AccountRepository accounts){ //injection
        this.accounts = accounts;
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
    
    //to be implemented in detail for sprint 2 (for managers)
    // @Override
    // public Book addAccount(Account account) {
    //     return accounts.save(account);
    // }
    
    // @Override
    // public void deleteAccount(Long id){
    //     accounts.deleteById(id);
    // }
}