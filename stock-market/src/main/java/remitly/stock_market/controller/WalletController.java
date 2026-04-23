package remitly.stock_market.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import remitly.stock_market.dto.AuditLogResponse;
import remitly.stock_market.dto.WalletStateResponse;
import remitly.stock_market.dto.WalletStockEntry;
import remitly.stock_market.service.TradingService;

import java.util.List;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final TradingService tradingService;

    public WalletController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @PostMapping("/{wallet_id}/stocks/{stock_name}")
    public void BuyOrSell(@PathVariable String wallet_id, @PathVariable String stock_name) {

    }

    @GetMapping("/{wallet_id}")
    public ResponseEntity<WalletStateResponse> returnWalletState(@PathVariable("wallet_id") int walletId) {
        List<WalletStockEntry> entries = tradingService.getWalletStocks(walletId);
        return ResponseEntity.ok(new WalletStateResponse(entries));
    }

    @GetMapping("/{wallet_id}/stocks/{stock_name}")
    public void getQuantityOfStock(@PathVariable("wallet_id") String walletId, @PathVariable("stock_name") String stockName) {
    }



}
