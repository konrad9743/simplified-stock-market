package remitly.stock_market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import remitly.stock_market.dto.AuditLogEntry;
import remitly.stock_market.model.entity.AuditLog;
import remitly.stock_market.model.enums.OperationType;
import remitly.stock_market.repository.AuditLogRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock AuditLogRepository auditLogRepository;
    @InjectMocks LogService logService;

    @Test
    void logIsEmptyWhenNothingHappened() {
        when(auditLogRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        assertThat(logService.getLogHistory()).isEmpty();
    }

    @Test
    void logEntryContainsAllTradeDetails() {
        when(auditLogRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                new AuditLog(OperationType.BUY, "wallet-1", "AAPL")
        ));

        AuditLogEntry entry = logService.getLogHistory().get(0);

        assertThat(entry.type()).isEqualTo(OperationType.BUY);
        assertThat(entry.walletId()).isEqualTo("wallet-1");
        assertThat(entry.stockName()).isEqualTo("AAPL");
    }

    @Test
    void logPreservesChronologicalOrder() {
        when(auditLogRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                new AuditLog(OperationType.BUY, "wallet-1", "AAPL"),
                new AuditLog(OperationType.SELL, "wallet-2", "GOOGL"),
                new AuditLog(OperationType.BUY, "wallet-1", "GOOGL")
        ));

        assertThat(logService.getLogHistory())
                .extracting(AuditLogEntry::type)
                .containsExactly(OperationType.BUY, OperationType.SELL, OperationType.BUY);
    }
}