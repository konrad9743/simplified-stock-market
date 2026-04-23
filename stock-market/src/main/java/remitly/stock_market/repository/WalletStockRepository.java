package remitly.stock_market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import remitly.stock_market.model.entity.WalletStock;

import java.util.List;
import java.util.Optional;

public interface WalletStockRepository extends JpaRepository<WalletStock, Long> {
    List<WalletStock> findByWalletId(String walletId);
    Optional<WalletStock> findByWalletIdAndStockName(String walletId, String stockName);
}