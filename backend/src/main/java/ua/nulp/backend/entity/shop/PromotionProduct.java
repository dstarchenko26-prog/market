package ua.nulp.backend.entity.shop;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_products", uniqueConstraints = {
        @UniqueConstraint(name = "uk_promo_product", columnNames = {"promotion_id", "product_id"})
})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    @ToString.Exclude
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    // === JPA ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromotionProduct that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}