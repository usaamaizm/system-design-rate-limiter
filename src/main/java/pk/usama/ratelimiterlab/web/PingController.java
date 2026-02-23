package pk.usama.ratelimiterlab.web;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {


    @GetMapping("/test")
    public String ping() {
        return "Pong";
    }
}
