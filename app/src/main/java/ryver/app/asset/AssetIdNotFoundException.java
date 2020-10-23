package ryver.app.asset;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // 404 Error
public class AssetIdNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public AssetIdNotFoundException(Long assetId) {
        super("Could not find asset " + assetId);
    }
    
}
