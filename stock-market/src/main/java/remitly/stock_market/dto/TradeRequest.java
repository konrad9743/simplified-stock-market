package remitly.stock_market.dto;

import remitly.stock_market.model.enums.OperationType;

public record TradeRequest(OperationType type) {
}
