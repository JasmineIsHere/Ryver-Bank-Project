//package empty;
package ryver.app.account;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class AccountController{
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService injectedAccountService){
        this.accountService = injectedAccountService;
    }

    //JOLENE:  we dont have SID anymore, and the book one had no UID
    // Checks SID if SID == 0. Get userID with getUserId(SID). 
    // Return all the accounts the UID possess.
    @GetMapping("/accounts")
    public List<Account> getAccounts(Long UID){ 
        return accountService.listAccounts(UID);
    }

    //Takes in the UID and SID of the session, and the account that he/she wishes to view. 
    //Check if the account selected is in the list of all AIDs the UID has, 
    //if yes, return AID of selected account, else return error.
    @GetMapping("/accounts/{AID}")
    public Account getAccount(Long UID){
        Account account = accountService.getAccount(UID);
        
        if(account == null){
            throw new AccountNotFoundException(UID);
        }

        return accountService.getAccount(UID);
    }

    //Takes in an AID, checks if the AID exists; 
    //if yes, returns the balance of the account, else return error.
    @GetMapping("/accounts/{AID}/balance")
    public double getBalance(Long AID){
        Account account = accountService.getAccount(AID);

        return account.getBalance();
    }

    // //JOLENE: is it accountService or transactionService, im for the latter
    // //Takes in an UID, checks if the UID exists; 
    // //if yes, returns list of transaction of the user, else return error.
    // @GetMapping("/accounts/{Aid}/transaction")
    // public List<Transaction> getUserTransaction(int UID){ 
    //         return accountService.listTransaction(UID);
    // }

    // //JOLENE:   I think getUserTransaction and getAccountTransaction is the same thing
    // //Takes in an AID and get userID with getUserID(SID), 
    // //checks if the AID exists in userID’s list of accounts; 
    // //if yes, returns list of transactions of the account, else return error.
    // @GetMapping("/accounts/{Aid}/transaction")
    // public List<Transaction> getAccountTransaction(int AID){
    //     return accountService.listTransaction(AID);
    // }

    // //Takes in an AID, checks if the AID exists; 
    // //if yes, it returns a list of investment in the account
    // @GetMapping("/accounts/{Aid}/investment")
    // public List<Investment> getInvestment(int AID){
    //     return accountService.listInvestment(AID);
    // }
}
