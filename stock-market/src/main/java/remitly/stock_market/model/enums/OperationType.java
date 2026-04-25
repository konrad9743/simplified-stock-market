package remitly.stock_market.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OperationType {
    BUY, SELL;

    @JsonValue
    public String toValue() { return name().toLowerCase(); }
}