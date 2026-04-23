package remitly.stock_market.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/log")
public class AuditController {
    @GetMapping
    public void getLogHistory() {

    }
}
