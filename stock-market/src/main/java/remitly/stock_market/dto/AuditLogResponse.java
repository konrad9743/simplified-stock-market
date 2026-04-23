package remitly.stock_market.dto;

import java.util.List;

public record AuditLogResponse(
        List<AuditLogEntry> log
) {}