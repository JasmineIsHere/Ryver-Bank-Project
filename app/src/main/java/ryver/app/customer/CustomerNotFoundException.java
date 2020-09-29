package ryver.app.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public CustomerNotFoundException(Long customerId) {
        super("Could not find customer " + customerId);
    }
    
}
