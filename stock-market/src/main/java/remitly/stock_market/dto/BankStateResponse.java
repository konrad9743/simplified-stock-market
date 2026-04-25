package remitly.stock_market.dto;

import java.util.List;

public record BankStateResponse(List<StockDto> stocks) {}