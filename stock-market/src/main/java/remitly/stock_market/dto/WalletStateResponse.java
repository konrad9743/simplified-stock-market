package remitly.stock_market.dto;

import java.util.ArrayList;
import java.util.List;

public record WalletStateResponse(List<WalletStockEntry> stocks) {
}
