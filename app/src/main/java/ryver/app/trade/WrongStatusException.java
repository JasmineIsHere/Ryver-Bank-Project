package ryver.app.trade;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Error
public class WrongStatusException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public WrongStatusException() {
        super("Your trade has already been filled/ partial-filled and you are not allowed to cancel it. ");
    }
    
}