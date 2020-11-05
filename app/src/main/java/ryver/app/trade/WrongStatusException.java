package ryver.app.trade;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Error
public class WrongStatusException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public WrongStatusException() {
        super("You can only cancel a trade that is open. ");
    }
    
}