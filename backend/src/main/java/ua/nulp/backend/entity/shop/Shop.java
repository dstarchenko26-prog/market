package ua.nulp.backend.entity.shop;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ua.nulp.backend.entity.enums.ShopStatus;
import ua.nulp.backend.entity.user.user.User;

import java.time.Instant;

@Entity
@Table(name = "shops", indexes = {
        @Index(name = "idx_shop_slug", columnList = "slug", unique = true),
        @Index(name = "idx_shop_owner", columnList = "owner_id"),
        @Index(name = "idx_shop_status", columnList = "status")
})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ShopStatus status = ShopStatus.DRAFT;

    @Column(nullable = false)
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    // === JPA ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shop shop)) return false;
        return id != null && id.equals(shop.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}