package remitly.stock_market.exception;

public class StockNotOwnedByWalletException extends RuntimeException {
    public StockNotOwnedByWalletException(String stockName, String walletId) {
        super("Wallet '" + walletId + "' does not own stock '" + stockName + "'");
    }
}