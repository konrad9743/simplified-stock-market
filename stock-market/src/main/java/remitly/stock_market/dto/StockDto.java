package remitly.stock_market.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockDto(
        @NotBlank(message = "Stock name cannot be empty")
        String name,

        @NotNull(message = "Quantity cannot be null")
        @Min(value = 0, message = "Quantity must be >= 0")
        Integer quantity
) {}