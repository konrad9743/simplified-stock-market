package remitly.stock_market.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BankStateRequest(
        @NotNull
        List<@Valid @NotNull StockDto> stocks
) {}