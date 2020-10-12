package ryver.app.stock;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) 
public class InvalidStockException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public InvalidStockException(String symbol) {
        super("Could not find the stock: " + symbol);
    }
    
}