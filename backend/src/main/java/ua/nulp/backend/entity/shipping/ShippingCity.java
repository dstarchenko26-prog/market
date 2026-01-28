package ua.nulp.backend.entity.shipping;

import jakarta.persistence.*;
import lombok.*;
import ua.nulp.backend.entity.enums.DeliveryProvider;

@Entity
@Table(name = "shipping_cities", uniqueConstraints = {
        @UniqueConstraint(name = "uk_city_provider_ext_id", columnNames = {"provider", "external_id"})
}, indexes = {
        @Index(name = "idx_city_name", columnList = "name"),
        @Index(name = "idx_city_provider", columnList = "provider")
})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingCity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryProvider provider;

    // Універсальний ID:
    // Для НП: "8d5a980d-391c-11dd-90d9-001a92567626" (UUID)
    // Для Укрпошти: "12345" (Long як String)
    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "region_name")
    private String regionName; // "Львівська область"

    @Column(name = "district_name")
    private String districtName; // "Стрийський район"

    // === JPA ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShippingCity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
