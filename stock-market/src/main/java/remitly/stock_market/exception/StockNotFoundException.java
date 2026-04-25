package remitly.stock_market.exception;


public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String stockName) {
        super("Stock '" + stockName + "' not found in Bank");
    }
}
