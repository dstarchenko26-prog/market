package ua.nulp.backend.entity.shop;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ua.nulp.backend.entity.enums.PromoScope;
import ua.nulp.backend.entity.enums.PromoType;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "promotions", indexes = {
        @Index(name = "idx_promo_shop", columnList = "shop_id"),
        @Index(name = "idx_promo_dates", columnList = "start_date, end_date"),
        @Index(name = "idx_promo_active", columnList = "is_active")
})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @ToString.Exclude
    private Shop shop;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromoType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromoScope scope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_category_id")
    @ToString.Exclude
    private Category targetCategory;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    public boolean isValidNow() {
        Instant now = Instant.now();
        return isActive && now.isAfter(startDate) && now.isBefore(endDate);
    }

    // === JPA ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Promotion promotion)) return false;
        return id != null && id.equals(promotion.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}