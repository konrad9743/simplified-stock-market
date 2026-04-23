package remitly.stock_market.service;

import org.springframework.stereotype.Service;
import remitly.stock_market.dto.AuditLogEntry;
import remitly.stock_market.model.entity.AuditLog;
import remitly.stock_market.repository.AuditLogRepository;

import java.util.List;

@Service
public class LogService {

    private final AuditLogRepository auditLogRepository;

    public LogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogEntry> getLogHistory() {
        List<AuditLog> logsFromDb = auditLogRepository.findAllByOrderByCreatedAtAsc();

        return logsFromDb.stream()
                .map(log -> new AuditLogEntry(
                        log.getType(),
                        log.getWalletId(),
                        log.getStockName()
                ))
                .toList();
    }
}
