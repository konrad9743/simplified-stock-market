package remitly.stock_market.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"walletId", "stockName"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String walletId;
    private String stockName;
    private int quantity;
}