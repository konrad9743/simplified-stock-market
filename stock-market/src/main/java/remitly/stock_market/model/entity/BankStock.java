package remitly.stock_market.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"walletId", "stockName"})
})
@Getter
@Setter
@NoArgsConstructor
public class BankStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private Integer quantity;

    public BankStock(String name, Integer quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}