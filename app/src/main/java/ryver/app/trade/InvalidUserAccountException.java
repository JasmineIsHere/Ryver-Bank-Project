package ryver.app.trade;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request
public class InvalidUserAccountException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public InvalidUserAccountException() {
        super("Invalid user account");
    }
    
}