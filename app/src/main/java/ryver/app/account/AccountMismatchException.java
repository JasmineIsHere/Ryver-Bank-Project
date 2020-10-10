package ryver.app.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "You cannot access this account")
public class AccountMismatchException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public AccountMismatchException() {
        super("You cannot access this account");
    }
    
}