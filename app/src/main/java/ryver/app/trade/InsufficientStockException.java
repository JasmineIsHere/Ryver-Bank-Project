package ryver.app.trade;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "There is insufficient stock in your portfolio")
public class InsufficientStockException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public InsufficientStockException() {
        super("There is insufficient stock in your portfolio");
    }
}
