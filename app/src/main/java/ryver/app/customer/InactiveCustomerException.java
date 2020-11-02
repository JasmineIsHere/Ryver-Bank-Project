package ryver.app.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "You are inactive ") // 401 Unauthorized
public class InactiveCustomerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InactiveCustomerException() {
        super("You are inactive ");
    }

}
