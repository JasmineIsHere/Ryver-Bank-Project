package ryver.app.trade;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // 404 Not Found
public class TradeNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public TradeNotFoundException(Long tradeId) {
        super("Could not find trade " + tradeId);
    }
    
}