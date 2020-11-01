package ryver.app.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "NRIC must be valid. ") // 400 Bad Request
public class InvalidNricException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidNricException() {
        super("NRIC must be valid. ");
    }

}
