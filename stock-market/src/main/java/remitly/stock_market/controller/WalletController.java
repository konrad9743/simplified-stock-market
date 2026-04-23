package remitly.stock_market.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    @PostMapping("/{wallet_id}/stocks/{stock_name}")
    public void BuyOrSell(@PathVariable String wallet_id, @PathVariable String stock_name) {

    }

    @GetMapping("/{wallet_id}")
    public void returnWalletState(@PathVariable String wallet_id) {
    }

    @GetMapping("/{wallet_id}/stocks/{stock_name}")
    public void getQuantityOfStock(@PathVariable("wallet_id") String walletId, @PathVariable("stock_name") String stockName) {
    }



}
