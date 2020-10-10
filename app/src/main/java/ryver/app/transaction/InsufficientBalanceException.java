package ryver.app.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Insufficient Balance in account ")
public class InsufficientBalanceException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public InsufficientBalanceException() {
        super("Insufficient Balance");
    }
    
}