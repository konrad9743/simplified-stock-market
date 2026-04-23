package remitly.stock_market.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import remitly.stock_market.model.enums.OperationType;

public record AuditLogEntry(
        OperationType type,
        @JsonProperty("wallet_id")
        String walletId,
        @JsonProperty("stock_name")
        String stockName
) {}
