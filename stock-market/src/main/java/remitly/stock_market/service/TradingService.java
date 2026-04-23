package remitly.stock_market.service;


import org.springframework.stereotype.Service;
import remitly.stock_market.dto.WalletStockEntry;
import remitly.stock_market.model.entity.WalletStock;
import remitly.stock_market.repository.WalletStockRepository;
import java.util.List;

@Service
public class TradingService {
    private final WalletStockRepository walletStockRepository;

    public TradingService(WalletStockRepository walletStockRepository) {
        this.walletStockRepository = walletStockRepository;
    }

    public List<WalletStockEntry> getWalletStocks(int walletId) {
        List<WalletStock> stocksFromDbs = walletStockRepository.findByWalletId(walletId);
        return stocksFromDbs.stream()
                .map(stocks -> new WalletStockEntry(
                        stocks.getStockName(),
                        stocks.getQuantity()
                ))
                .toList();
    }
}
