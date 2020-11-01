package ryver.app.trade;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "This is an invalid trade. Please specify action as either 'buy' or 'sell'.") // 400 Bad Request
public class InvalidTradeException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public InvalidTradeException() {
        super("This is an invalid trade. Please specify action as either 'buy' or 'sell'.");
    }
}
