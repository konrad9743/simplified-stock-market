package remitly.stock_market.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String stockName, String walletId) {
        super("Wallet '" + walletId + "' has insufficient quantity of stock '" + stockName + "'");
    }
}