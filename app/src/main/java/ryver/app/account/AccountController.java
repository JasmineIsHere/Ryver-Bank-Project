//package empty;
package ryver.app.account;

import java.util.List;
import java.util.Optional;
import ryver.app.user.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

import ryver.app.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class AccountController{
    private AccountRepository accounts;
    private UserRepository users;

    @Autowired
    public AccountController(AccountRepository accounts){
        this.accounts = accounts;
    }

    //JOLENE:  we dont have SID anymore, and the book one had no UID
    // Checks SID if SID == 0. Get userID with getUserId(SID). 
    // Return all the accounts the UID possess.
    @GetMapping("/users/{UID}/accounts")
    public List<Account> getAllAccountsByUID(@PathVariable (value = "UID") Long UID){ 
        if(!users.existsById(UID)) {
            throw new UserNotFoundException(UID);
        }
        return accounts.findByUID(UID);
    }

    //Takes in the UID and SID of the session, and the account that he/she wishes to view. 
    //Check if the account selected is in the list of all AIDs the UID has, 
    //if yes, return AID of selected account, else return error.
    @GetMapping("/users/{UID}/accounts/{AID}")
    public Account getAccountByAIDAndUID(@PathVariable (value = "UID") Long UID, @PathVariable (value = "AID") Long AID){
        if(!users.existsById(UID)) {
            throw new UserNotFoundException(UID);
        }
        return accounts.findByIdAndUID(AID, UID).orElseThrow(() -> new AccountNotFoundException(AID));
    }

    // //Takes in an AID, checks if the AID exists; 
    // //if yes, returns the balance of the account, else return error.
    // @GetMapping("/accounts/{AID}/balance")
    // public double getBalance(Long AID){
    //     Account account = accounts.getAccount(AID);

    //     return account.getBalance();
    // }

    @PostMapping("/users/{UID}/accounts")
    public Account addAccount(@PathVariable (value = "UID") Long UID, @Valid @RequestBody Account account) {
        // using "map" to handle the returned Optional object from "findById(bookId)"
        return users.findById(UID).map(user ->{
            account.setUser(user);
            return accounts.save(account);
        }).orElseThrow(() -> new UserNotFoundException(UID));
    }

    // //JOLENE: is it accounts or transactionService, im for the latter
    // //Takes in an UID, checks if the UID exists; 
    // //if yes, returns list of transaction of the user, else return error.
    // @GetMapping("/accounts/{Aid}/transaction")
    // public List<Transaction> getUserTransaction(int UID){ 
    //         return accounts.listTransaction(UID);
    // }

    // //JOLENE:   I think getUserTransaction and getAccountTransaction is the same thing
    // //Takes in an AID and get userID with getUserID(SID), 
    // //checks if the AID exists in userID’s list of accounts; 
    // //if yes, returns list of transactions of the account, else return error.
    // @GetMapping("/accounts/{Aid}/transaction")
    // public List<Transaction> getAccountTransaction(int AID){
    //     return accounts.listTransaction(AID);
    // }

    // //Takes in an AID, checks if the AID exists; 
    // //if yes, it returns a list of investment in the account
    // @GetMapping("/accounts/{Aid}/investment")
    // public List<Investment> getInvestment(int AID){
    //     return accounts.listInvestment(AID);
    // }
}
