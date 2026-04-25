package remitly.stock_market.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import remitly.stock_market.dto.BankStateRequest;
import remitly.stock_market.dto.BankStateResponse;
import remitly.stock_market.dto.StockDto;
import remitly.stock_market.model.entity.BankStock;
import remitly.stock_market.repository.BankStockRepository;

import java.util.List;

@Service
public class BankService {
    private final BankStockRepository bankStockRepository;

    public BankService(BankStockRepository bankStockRepository) {
        this.bankStockRepository = bankStockRepository;
    }

    public BankStateResponse getBankState() {
        List<StockDto> stocks = bankStockRepository.findAll().stream()
                .map(s -> new StockDto(s.getName(), s.getQuantity()))
                .toList();

        return new BankStateResponse(stocks);
    }

    @Transactional
    public void setBankState(BankStateRequest request) {
        bankStockRepository.deleteAll();
        List<BankStock> newStocks = request.stocks().stream()
                .map(dto -> BankStock.builder()
                        .name(dto.name())
                        .quantity(dto.quantity())
                        .build())
                .toList();

        bankStockRepository.saveAll(newStocks);
    }
}
