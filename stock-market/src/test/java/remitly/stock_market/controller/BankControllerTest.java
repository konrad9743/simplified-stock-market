package remitly.stock_market.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import remitly.stock_market.dto.BankStateRequest;
import remitly.stock_market.dto.BankStateResponse;
import remitly.stock_market.dto.StockDto;
import remitly.stock_market.service.BankService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankController.class)
class BankControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean BankService bankService;

    @Test
    void freshBankHasNoStocks() throws Exception {
        when(bankService.getBankState()).thenReturn(new BankStateResponse(List.of()));

        mockMvc.perform(get("/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stocks").isEmpty());
    }

    @Test
    void bankReturnsAllAvailableStocks() throws Exception {
        when(bankService.getBankState()).thenReturn(new BankStateResponse(List.of(
                new StockDto("AAPL", 99),
                new StockDto("GOOGL", 1)
        )));

        mockMvc.perform(get("/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stocks.length()").value(2))
                .andExpect(jsonPath("$.stocks[0].name").value("AAPL"))
                .andExpect(jsonPath("$.stocks[0].quantity").value(99));
    }

    @Test
    void settingBankStateReplacesEverything() throws Exception {
        doNothing().when(bankService).setBankState(any());

        mockMvc.perform(post("/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BankStateRequest(List.of(new StockDto("AAPL", 100)))
                        )))
                .andExpect(status().isOk());

        verify(bankService).setBankState(any(BankStateRequest.class));
    }

    @Test
    void cannotSetNegativeStockQuantity() throws Exception {
        // Nie mockujemy serwisu! Spring Validation zablokuje to na poziomie kontrolera.
        mockMvc.perform(post("/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BankStateRequest(List.of(new StockDto("AAPL", -1)))
                        )))
                .andExpect(status().isBadRequest());

        // Upewniamy się, że żądanie zablokowano ZANIM dotarło do serwisu
        verify(bankService, never()).setBankState(any());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid}"))
                .andExpect(status().isBadRequest());

        verify(bankService, never()).setBankState(any());
    }
}