// package csd.week5.book;
package ryver.app.account;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException{

    private static final long serialVersionAID = 1L;

    public AccountNotFoundException(Long AID) {
        super("Could not find account " + AID);
    }
    
}
