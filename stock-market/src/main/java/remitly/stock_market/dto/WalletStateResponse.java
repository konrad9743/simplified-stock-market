package remitly.stock_market.dto;

import java.util.List;

public record WalletStateResponse(List<WalletStockEntry> stocks) {
}
