package remitly.stock_market.exception;

public class InvalidStockQuantityException extends RuntimeException {
    public InvalidStockQuantityException(String stockName, int quantity) {
        super("Invalid quantity " + quantity + " for stock '" + stockName + "': must be >= 0");
    }
}
