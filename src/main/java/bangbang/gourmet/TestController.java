package bangbang.gourmet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hi")
    public ResponseEntity<String> test(){
        return ResponseEntity.accepted().body("ok");
    }
}
