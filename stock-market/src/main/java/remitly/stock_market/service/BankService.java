package remitly.stock_market.service;

import org.springframework.stereotype.Service;
import remitly.stock_market.dto.BankStateResponse;
import remitly.stock_market.dto.StockDto;
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
}
