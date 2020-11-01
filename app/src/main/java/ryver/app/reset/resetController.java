package ryver.app.reset;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import ryver.app.AppApplication;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class resetController {

    /**
     * To reset the API during testing
     */
    @PostMapping("/api/reset")
    public void restart() {
        AppApplication.restart();
    } 

}
