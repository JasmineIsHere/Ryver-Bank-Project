package ryver.app.trade;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "The quantity should be in multiples of 100") // 400 Bad Request
public class InvalidQuantityException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public InvalidQuantityException() {
        super("The quantity should be in multiples of 100");
    }
}
