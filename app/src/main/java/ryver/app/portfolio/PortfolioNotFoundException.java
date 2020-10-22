package ryver.app.portfolio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PortfolioNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public PortfolioNotFoundException(Long customerId) {
        super("Could not find portfolio for customer " + customerId);
    }
    
}
