package remitly.stock_market.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import remitly.stock_market.dto.BankStateRequest;
import remitly.stock_market.dto.BankStateResponse;
import remitly.stock_market.service.BankService;

@RestController
@RequestMapping("/stocks")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping
    public ResponseEntity<BankStateResponse> getBankState() {
        return ResponseEntity.ok(bankService.getBankState());
    }

    @PostMapping
    public ResponseEntity<Void> setBankState(@RequestBody BankStateRequest request) {
        bankService.setBankState(request);
        return ResponseEntity.ok().build();
    }
}