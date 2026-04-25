package remitly.stock_market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import remitly.stock_market.dto.AuditLogEntry;
import remitly.stock_market.model.entity.AuditLog;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByIdAsc();
}