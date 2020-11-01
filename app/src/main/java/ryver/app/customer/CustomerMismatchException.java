package ryver.app.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "You cannot access this customer account") // 403 Forbidden
public class CustomerMismatchException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public CustomerMismatchException() {
        super("You cannot access this customer account");
    }
    
}