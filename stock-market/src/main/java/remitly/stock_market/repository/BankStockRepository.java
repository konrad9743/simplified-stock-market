package remitly.stock_market.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import remitly.stock_market.model.entity.BankStock;
import java.util.Optional;

public interface BankStockRepository extends JpaRepository<BankStock, Long> {
    Optional<BankStock> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankStock b WHERE b.name = :name")
    Optional<BankStock> findByNameWithLock(String name);
}