package ryver.app.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UsernameAlreadyExistException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public UsernameAlreadyExistException(String username) {
        super("Username " + username + " is in used. Please choose another username");
    }
}
