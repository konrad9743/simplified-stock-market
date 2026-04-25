package remitly.stock_market.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import remitly.stock_market.model.entity.WalletStock;

import java.util.List;
import java.util.Optional;

public interface WalletStockRepository extends JpaRepository<WalletStock, Long> {
    List<WalletStock> findByWalletId(String walletId);

    @Query("SELECT w.quantity FROM WalletStock w WHERE w.walletId = :walletId AND w.stockName = :stockName")
    Optional<Integer> findQuantityByWalletIdAndStockName(String walletId, String stockName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletStock w WHERE w.walletId = :walletId AND w.stockName = :stockName")
    Optional<WalletStock> findByWalletIdAndStockNameWithLock(String walletId, String stockName);
}