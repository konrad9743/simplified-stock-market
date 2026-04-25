package remitly.stock_market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import remitly.stock_market.dto.AuditLogEntry;
import remitly.stock_market.repository.AuditLogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLogEntry> getLogHistory() {
        return auditLogRepository.findAllByOrderByIdAsc().stream()
                .map(log -> new AuditLogEntry(log.getType(), log.getWalletId(), log.getStockName()))
                .toList();
    }
}