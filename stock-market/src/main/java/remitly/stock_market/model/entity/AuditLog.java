package remitly.stock_market.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import remitly.stock_market.model.enums.OperationType;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OperationType type;

    private String walletId;

    private String stockName;

    private Instant createdAt = Instant.now();

    public AuditLog(OperationType type, String walletId, String stockName) {
        this.type = type;
        this.walletId = walletId;
        this.stockName = stockName;
        this.createdAt = Instant.now();
    }

}
