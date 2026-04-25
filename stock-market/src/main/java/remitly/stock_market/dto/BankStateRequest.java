package remitly.stock_market.dto;

import java.util.List;

public record BankStateRequest(List<StockDto> stocks) {
    
}
