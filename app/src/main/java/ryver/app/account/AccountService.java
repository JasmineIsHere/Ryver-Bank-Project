package ryver.app.account;

import java.util.List;

public interface AccountService {
    List<Account> listAccounts(Long UID);
    Account getAccount(Long AID);
    Account addAccount(long user,long accountId,double balance,double availBalance);    
}