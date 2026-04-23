package remitly.stock_market.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class WalletStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String walletId;

    private String stockName;

    private Integer quantity;

    public WalletStock(String walletId, String stockName, Integer quantity) {
        this.walletId = walletId;
        this.stockName = stockName;
        this.quantity = quantity;
    }
}