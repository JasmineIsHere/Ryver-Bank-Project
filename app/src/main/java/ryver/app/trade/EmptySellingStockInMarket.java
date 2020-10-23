package ryver.app.trade;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class EmptySellingStockInMarket extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmptySellingStockInMarket() {
        super("The market does not have any more stocks to be sold");
    }
    
}
