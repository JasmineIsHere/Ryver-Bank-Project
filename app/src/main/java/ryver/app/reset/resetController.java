package ryver.app.reset;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import ryver.app.AppApplication;

@RestController
public class resetController {

    @PostMapping("/reset")
    public void restart() {
        AppApplication.restart();
    } 

}
