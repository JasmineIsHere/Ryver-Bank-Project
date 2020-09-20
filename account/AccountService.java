//package csd.sprint1.account; //to be updated based on directory

import java.util.List;

public interface AccountService {
    List<Account> listAccounts();
    Book getAccount(Long id);

    //Book addAccount(Account account); //for manager //to be included for sprint 2

    /**
     * Change method's signature: do not return a value for delete operation
     * @param id
     */
    //void deleteAccount(Long id); //for manager // to be included for sprint 2
}