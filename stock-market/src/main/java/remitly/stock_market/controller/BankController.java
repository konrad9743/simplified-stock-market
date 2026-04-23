package remitly.stock_market.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/stocks")
public class BankController {
    @GetMapping
    public void getBankState() {
    }

    @PostMapping
    public void setBankState() {

    }
}
