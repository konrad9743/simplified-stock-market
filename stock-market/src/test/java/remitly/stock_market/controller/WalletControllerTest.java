package remitly.stock_market.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import remitly.stock_market.dto.TradeRequest;
import remitly.stock_market.dto.WalletStockEntry;
import remitly.stock_market.exception.*;
import remitly.stock_market.model.enums.OperationType;
import remitly.stock_market.service.TradingService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean TradingService tradingService;

    // --- POST /wallets/{wallet_id}/stocks/{stock_name} ---

    @Test
    void buyingStockSucceeds() throws Exception {
        doNothing().when(tradingService).executeTrade(any(), any(), any());

        mockMvc.perform(post("/wallets/wallet-1/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TradeRequest(OperationType.BUY))))
                .andExpect(status().isOk());

        verify(tradingService).executeTrade(eq("wallet-1"), eq("AAPL"), any());
    }

    @Test
    void sellingStockSucceeds() throws Exception {
        doNothing().when(tradingService).executeTrade(any(), any(), any());

        mockMvc.perform(post("/wallets/wallet-1/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TradeRequest(OperationType.SELL))))
                .andExpect(status().isOk());
    }

    @Test
    void cannotTradeStockThatDoesNotExistInMarket() throws Exception {
        doThrow(new StockNotFoundException("UNKNOWN"))
                .when(tradingService).executeTrade(any(), any(), any());

        mockMvc.perform(post("/wallets/wallet-1/stocks/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TradeRequest(OperationType.BUY))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void cannotBuyWhenBankIsOutOfStock() throws Exception {
        doThrow(new InsufficientBankStockException("AAPL"))
                .when(tradingService).executeTrade(any(), any(), any());

        mockMvc.perform(post("/wallets/wallet-1/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TradeRequest(OperationType.BUY))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void cannotSellStockYouDontOwn() throws Exception {
        doThrow(new StockNotOwnedByWalletException("AAPL", "wallet-1"))
                .when(tradingService).executeTrade(any(), any(), any());

        mockMvc.perform(post("/wallets/wallet-1/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TradeRequest(OperationType.SELL))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- GET /wallets/{wallet_id} ---

    @Test
    void newWalletHasNoStocks() throws Exception {
        when(tradingService.getWalletStocks("wallet-1")).thenReturn(List.of());

        mockMvc.perform(get("/wallets/wallet-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stocks").isEmpty());
    }

    @Test
    void walletShowsAllOwnedStocks() throws Exception {
        when(tradingService.getWalletStocks("wallet-1")).thenReturn(List.of(
                new WalletStockEntry("AAPL", 5),
                new WalletStockEntry("GOOGL", 2)
        ));

        mockMvc.perform(get("/wallets/wallet-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stocks.length()").value(2))
                .andExpect(jsonPath("$.stocks[0].name").value("AAPL"))
                .andExpect(jsonPath("$.stocks[0].quantity").value(5));
    }

    // --- GET /wallets/{wallet_id}/stocks/{stock_name} ---

    @Test
    void stockQuantityIsReturnedAsPlainNumber() throws Exception {
        when(tradingService.getStockQuantity("wallet-1", "AAPL")).thenReturn(99);

        mockMvc.perform(get("/wallets/wallet-1/stocks/AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string("99"));
    }

    @Test
    void checkingQuantityOfUnownedStockReturns404() throws Exception {
        when(tradingService.getStockQuantity("wallet-1", "AAPL"))
                .thenThrow(new StockNotFoundException("AAPL"));

        mockMvc.perform(get("/wallets/wallet-1/stocks/AAPL"))
                .andExpect(status().isNotFound());
    }
}