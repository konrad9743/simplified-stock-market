package remitly.stock_market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import remitly.stock_market.model.entity.BankStock;
import java.util.Optional;

public interface BankStockRepository extends JpaRepository<BankStock, Long> {
    Optional<BankStock> findByName(String name);
}