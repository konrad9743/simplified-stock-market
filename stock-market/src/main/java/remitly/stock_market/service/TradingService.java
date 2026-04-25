package remitly.stock_market.service;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import remitly.stock_market.dto.WalletStockEntry;
import remitly.stock_market.model.entity.WalletStock;
import remitly.stock_market.repository.WalletStockRepository;
import java.util.List;
import java.util.Optional;

@Service
public class TradingService {
    private final WalletStockRepository walletStockRepository;

    public TradingService(WalletStockRepository walletStockRepository) {
        this.walletStockRepository = walletStockRepository;
    }

    public List<WalletStockEntry> getWalletStocks(String walletId) {
        List<WalletStock> stocksFromDbs = walletStockRepository.findByWalletId(walletId);
        return stocksFromDbs.stream()
                .map(stock -> new WalletStockEntry(
                        stock.getStockName(),
                        stock.getQuantity()
                ))
                .toList();
    }

    public int getStockQuantity(String walletId, String stockName) {
        return walletStockRepository.findQuantityByWalletIdAndStockName(walletId, stockName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Couldn't find " + stockName + " in wallet " + walletId
                ));
    }
}
