package remitly.stock_market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import remitly.stock_market.dto.BankStateRequest;
import remitly.stock_market.dto.BankStateResponse;
import remitly.stock_market.dto.StockDto;
import remitly.stock_market.model.entity.BankStock;
import remitly.stock_market.repository.BankStockRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock BankStockRepository bankStockRepository;
    @InjectMocks BankService bankService;

    @Test
    void freshBankHasNoStocks() {
        when(bankStockRepository.findAll()).thenReturn(List.of());

        assertThat(bankService.getBankState().stocks()).isEmpty();
    }

    @Test
    void bankReturnsCurrentStockLevels() {
        when(bankStockRepository.findAll()).thenReturn(List.of(
                bankStock("AAPL", 10),
                bankStock("GOOGL", 5)
        ));

        BankStateResponse response = bankService.getBankState();

        assertThat(response.stocks())
                .extracting(StockDto::name, StockDto::quantity)
                .containsExactly(
                        tuple("AAPL", 10),
                        tuple("GOOGL", 5)
                );
    }

    @Test
    void settingBankStateReplacesEverything() {
        bankService.setBankState(new BankStateRequest(List.of(
                new StockDto("AAPL", 100),
                new StockDto("GOOGL", 50)
        )));

        verify(bankStockRepository).deleteAll();
        verify(bankStockRepository).flush();

        ArgumentCaptor<List<BankStock>> captor = ArgumentCaptor.forClass(List.class);
        verify(bankStockRepository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .extracting(BankStock::getName, BankStock::getQuantity)
                .containsExactly(tuple("AAPL", 100), tuple("GOOGL", 50));
    }

    @Test
    void zeroQuantityIsValidWhenSettingBankState() {
        assertThatNoException().isThrownBy(() ->
                bankService.setBankState(new BankStateRequest(List.of(new StockDto("AAPL", 0))))
        );
    }

    private BankStock bankStock(String name, int quantity) {
        return BankStock.builder().name(name).quantity(quantity).build();
    }
}