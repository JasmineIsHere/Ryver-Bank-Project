package ryver.app.content;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // 404 Not Found
public class ContentNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public ContentNotFoundException(Long contentId) {
        super("Could not find Content ID " + contentId);
    }
    
}