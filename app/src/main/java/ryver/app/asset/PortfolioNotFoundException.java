package ryver.app.asset;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // 404 Error
public class PortfolioNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PortfolioNotFoundException(Long portfolioId) {
        super("Could not find portfolio " + portfolioId);
    }
    
}
