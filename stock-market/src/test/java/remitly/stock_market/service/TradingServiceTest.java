package remitly.stock_market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import remitly.stock_market.dto.TradeRequest;
import remitly.stock_market.dto.WalletStockEntry;
import remitly.stock_market.exception.*;
import remitly.stock_market.model.entity.AuditLog;
import remitly.stock_market.model.entity.BankStock;
import remitly.stock_market.model.entity.WalletStock;
import remitly.stock_market.model.enums.OperationType;
import remitly.stock_market.repository.AuditLogRepository;
import remitly.stock_market.repository.BankStockRepository;
import remitly.stock_market.repository.WalletStockRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock WalletStockRepository walletStockRepository;
    @Mock BankStockRepository bankStockRepository;
    @Mock AuditLogRepository auditLogRepository;
    @InjectMocks TradingService tradingService;

    // --- getWalletStocks ---

    @Test
    void newWalletHasNoStocks() {
        when(walletStockRepository.findByWalletIdAndQuantityGreaterThan("wallet-1", 0))
                .thenReturn(List.of());

        assertThat(tradingService.getWalletStocks("wallet-1")).isEmpty();
    }

    @Test
    void walletShowsCurrentHoldings() {
        when(walletStockRepository.findByWalletIdAndQuantityGreaterThan("wallet-1", 0))
                .thenReturn(List.of(walletStock("wallet-1", "AAPL", 5)));

        List<WalletStockEntry> holdings = tradingService.getWalletStocks("wallet-1");

        assertThat(holdings).extracting(WalletStockEntry::name, WalletStockEntry::quantity)
                .containsExactly(tuple("AAPL", 5));
    }

    // --- getStockQuantity ---

    @Test
    void returnsQuantityOfOwnedStock() {
        when(walletStockRepository.findQuantityByWalletIdAndStockName("wallet-1", "AAPL"))
                .thenReturn(Optional.of(7));

        assertThat(tradingService.getStockQuantity("wallet-1", "AAPL")).isEqualTo(7);
    }

    @Test
    void checkingQuantityOfUnownedStockThrows() {
        when(walletStockRepository.findQuantityByWalletIdAndStockName("wallet-1", "AAPL"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradingService.getStockQuantity("wallet-1", "AAPL"))
                .isInstanceOf(StockNotFoundException.class);
    }

    // --- BUY ---

    @Test
    void buyingStockTransfersItFromBankToWallet() {
        BankStock bank = bankStock("AAPL", 10);
        WalletStock wallet = walletStock("wallet-1", "AAPL", 3);
        when(bankStockRepository.findByNameWithLock("AAPL")).thenReturn(Optional.of(bank));
        when(walletStockRepository.findByWalletIdAndStockNameWithLock("wallet-1", "AAPL"))
                .thenReturn(Optional.of(wallet));

        tradingService.executeTrade("wallet-1", "AAPL", new TradeRequest(OperationType.BUY));

        assertThat(bank.getQuantity()).isEqualTo(9);
        assertThat(wallet.getQuantity()).isEqualTo(4);
    }

    @Test
    void buyingCreatesWalletEntryWhenStockNotYetOwned() {
        when(bankStockRepository.findByNameWithLock("AAPL"))
                .thenReturn(Optional.of(bankStock("AAPL", 5)));
        when(walletStockRepository.findByWalletIdAndStockNameWithLock("new-wallet", "AAPL"))
                .thenReturn(Optional.empty());

        tradingService.executeTrade("new-wallet", "AAPL", new TradeRequest(OperationType.BUY));

        ArgumentCaptor<WalletStock> captor = ArgumentCaptor.forClass(WalletStock.class);
        verify(walletStockRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(1);
        assertThat(captor.getValue().getWalletId()).isEqualTo("new-wallet");
    }

    @Test
    void cannotBuyStockThatDoesNotExistInMarket() {
        when(bankStockRepository.findByNameWithLock("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                tradingService.executeTrade("wallet-1", "UNKNOWN", new TradeRequest(OperationType.BUY))
        ).isInstanceOf(StockNotFoundException.class);

        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void cannotBuyWhenBankIsOutOfStock() {
        when(bankStockRepository.findByNameWithLock("AAPL"))
                .thenReturn(Optional.of(bankStock("AAPL", 0)));

        assertThatThrownBy(() ->
                tradingService.executeTrade("wallet-1", "AAPL", new TradeRequest(OperationType.BUY))
        ).isInstanceOf(InsufficientBankStockException.class);

        verify(auditLogRepository, never()).save(any());
    }

    // --- SELL ---

    @Test
    void sellingStockReturnsItToBank() {
        BankStock bank = bankStock("AAPL", 0);
        WalletStock wallet = walletStock("wallet-1", "AAPL", 3);
        when(bankStockRepository.findByNameWithLock("AAPL")).thenReturn(Optional.of(bank));
        when(walletStockRepository.findByWalletIdAndStockNameWithLock("wallet-1", "AAPL"))
                .thenReturn(Optional.of(wallet));

        tradingService.executeTrade("wallet-1", "AAPL", new TradeRequest(OperationType.SELL));

        assertThat(bank.getQuantity()).isEqualTo(1);
        assertThat(wallet.getQuantity()).isEqualTo(2);
    }

    @Test
    void sellingLastShareRemovesStockFromWallet() {
        when(bankStockRepository.findByNameWithLock("AAPL"))
                .thenReturn(Optional.of(bankStock("AAPL", 0)));
        when(walletStockRepository.findByWalletIdAndStockNameWithLock("wallet-1", "AAPL"))
                .thenReturn(Optional.of(walletStock("wallet-1", "AAPL", 1)));

        tradingService.executeTrade("wallet-1", "AAPL", new TradeRequest(OperationType.SELL));

        verify(walletStockRepository).delete(any(WalletStock.class));
        verify(walletStockRepository, never()).save(any());
    }

    @Test
    void cannotSellStockYouDontOwn() {
        when(bankStockRepository.findByNameWithLock("AAPL"))
                .thenReturn(Optional.of(bankStock("AAPL", 0)));
        when(walletStockRepository.findByWalletIdAndStockNameWithLock("wallet-1", "AAPL"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                tradingService.executeTrade("wallet-1", "AAPL", new TradeRequest(OperationType.SELL))
        ).isInstanceOf(StockNotOwnedByWalletException.class);

        verify(auditLogRepository, never()).save(any());
    }

    // --- Audit log ---

    @Test
    void successfulTradeIsAlwaysLogged() {
        when(bankStockRepository.findByNameWithLock("AAPL"))
                .thenReturn(Optional.of(bankStock("AAPL", 5)));
        when(walletStockRepository.findByWalletIdAndStockNameWithLock("wallet-1", "AAPL"))
                .thenReturn(Optional.of(walletStock("wallet-1", "AAPL", 1)));

        tradingService.executeTrade("wallet-1", "AAPL", new TradeRequest(OperationType.BUY));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(OperationType.BUY);
        assertThat(captor.getValue().getWalletId()).isEqualTo("wallet-1");
        assertThat(captor.getValue().getStockName()).isEqualTo("AAPL");
    }

    @Test
    void failedTradeIsNeverLogged() {
        when(bankStockRepository.findByNameWithLock("AAPL"))
                .thenReturn(Optional.of(bankStock("AAPL", 0)));

        assertThatThrownBy(() ->
                tradingService.executeTrade("wallet-1", "AAPL", new TradeRequest(OperationType.BUY))
        ).isInstanceOf(InsufficientBankStockException.class);

        verify(auditLogRepository, never()).save(any());
    }

    // --- helpers ---

    private BankStock bankStock(String name, int quantity) {
        return BankStock.builder().name(name).quantity(quantity).build();
    }

    private WalletStock walletStock(String walletId, String name, int quantity) {
        return WalletStock.builder().walletId(walletId).stockName(name).quantity(quantity).build();
    }
}