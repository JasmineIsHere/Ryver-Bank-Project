package ryver.app.asset;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // 404 Error
public class AssetNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AssetNotFoundException(Long assetId) {
        super("Could not find asset ID " + assetId);
    }
    
}
