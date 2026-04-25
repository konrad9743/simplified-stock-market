package remitly.stock_market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import remitly.stock_market.dto.TradeRequest;
import remitly.stock_market.dto.WalletStockEntry;
import remitly.stock_market.exception.*;
import remitly.stock_market.model.entity.AuditLog;
import remitly.stock_market.model.entity.BankStock;
import remitly.stock_market.model.entity.WalletStock;
import remitly.stock_market.model.enums.OperationType;
import remitly.stock_market.repository.AuditLogRepository;
import remitly.stock_market.repository.BankStockRepository;
import remitly.stock_market.repository.WalletStockRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradingService {

    private final WalletStockRepository walletStockRepository;
    private final BankStockRepository bankStockRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<WalletStockEntry> getWalletStocks(String walletId) {
        return walletStockRepository.findByWalletIdAndQuantityGreaterThan(walletId, 0).stream()
                .map(stock -> new WalletStockEntry(stock.getStockName(), stock.getQuantity()))
                .toList();
    }

    @Transactional(readOnly = true)
    public int getStockQuantity(String walletId, String stockName) {
        return walletStockRepository.findQuantityByWalletIdAndStockName(walletId, stockName)
                .orElseThrow(() -> new StockNotFoundException(stockName));
    }

    @Transactional
    public void executeTrade(String walletId, String stockName, TradeRequest request) {
        BankStock bankStock = bankStockRepository.findByNameWithLock(stockName)
                .orElseThrow(() -> new StockNotFoundException(stockName));

        if (request.type() == OperationType.BUY) {
            handleBuy(walletId, bankStock);
        } else {
            handleSell(walletId, bankStock);
        }

        auditLogRepository.save(new AuditLog(request.type(), walletId, stockName));
    }

    private void handleBuy(String walletId, BankStock bankStock) {
        if (bankStock.getQuantity() <= 0) {
            throw new InsufficientBankStockException(bankStock.getName());
        }

        bankStock.setQuantity(bankStock.getQuantity() - 1);
        bankStockRepository.save(bankStock);

        WalletStock walletStock = walletStockRepository
                .findByWalletIdAndStockNameWithLock(walletId, bankStock.getName())
                .orElse(WalletStock.builder()
                        .walletId(walletId)
                        .stockName(bankStock.getName())
                        .quantity(0)
                        .build());

        walletStock.setQuantity(walletStock.getQuantity() + 1);
        walletStockRepository.save(walletStock);
    }

    private void handleSell(String walletId, BankStock bankStock) {
        WalletStock walletStock = walletStockRepository
                .findByWalletIdAndStockNameWithLock(walletId, bankStock.getName())
                .orElseThrow(() -> new StockNotOwnedByWalletException(bankStock.getName(), walletId));

        if (walletStock.getQuantity() <= 0) {
            throw new InsufficientStockException(bankStock.getName(), walletId);
        }

        walletStock.setQuantity(walletStock.getQuantity() - 1);

        if (walletStock.getQuantity() == 0) {
            walletStockRepository.delete(walletStock);
        } else {
            walletStockRepository.save(walletStock);
        }

        bankStock.setQuantity(bankStock.getQuantity() + 1);
        bankStockRepository.save(bankStock);
    }
}