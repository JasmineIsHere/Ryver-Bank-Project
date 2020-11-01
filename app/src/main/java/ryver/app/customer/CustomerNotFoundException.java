package ryver.app.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request
public class CustomerNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CustomerNotFoundException(Long customerId) {
        super("Could not find customer ID" + customerId);
    }

    public CustomerNotFoundException(String customerName) {
        super("Could not find customer " + customerName);
    }

}
