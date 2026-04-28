package remitly.stock_market.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import remitly.stock_market.dto.AuditLogEntry;
import remitly.stock_market.model.enums.OperationType;
import remitly.stock_market.service.LogService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean LogService logService;

    @Test
    void logIsEmptyAtTheStart() throws Exception {
        when(logService.getLogHistory()).thenReturn(List.of());

        mockMvc.perform(get("/log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log").isEmpty());
    }

    @Test
    void logReturnsEntriesInChronologicalOrder() throws Exception {
        when(logService.getLogHistory()).thenReturn(List.of(
                new AuditLogEntry(OperationType.BUY, "wallet-1", "AAPL"),
                new AuditLogEntry(OperationType.SELL, "wallet-2", "GOOGL")
        ));

        mockMvc.perform(get("/log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log.length()").value(2))
                .andExpect(jsonPath("$.log[0].type").value("buy"))
                .andExpect(jsonPath("$.log[0].wallet_id").value("wallet-1"))
                .andExpect(jsonPath("$.log[0].stock_name").value("AAPL"))
                .andExpect(jsonPath("$.log[1].type").value("sell"));
    }

    @Test
    void operationTypeIsLowercaseInResponse() throws Exception {
        when(logService.getLogHistory()).thenReturn(List.of(
                new AuditLogEntry(OperationType.BUY, "wallet-1", "AAPL")
        ));

        mockMvc.perform(get("/log"))
                .andExpect(jsonPath("$.log[0].type").value("buy"));
    }
}