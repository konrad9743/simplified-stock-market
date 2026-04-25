package remitly.stock_market.exception;

public class InsufficientBankStockException extends RuntimeException {
    public InsufficientBankStockException(String stockName) {
        super("No stock '" + stockName + "' available in Bank");
    }
}