package ua.nulp.backend.entity.delivery;

import jakarta.persistence.*;
import lombok.*;
import ua.nulp.backend.entity.enums.DeliveryProvider;

@Entity
@Table(name = "shipping_branches", uniqueConstraints = {
        @UniqueConstraint(name = "uk_branch_provider_ext_id", columnNames = {"provider", "external_id"})
}, indexes = {
        @Index(name = "idx_branch_city", columnList = "city_id"),
        @Index(name = "idx_branch_provider", columnList = "provider")
})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryProvider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    @ToString.Exclude
    private ShippingCity city;

    // Зовнішній ID відділення
    @Column(name = "external_id", nullable = false)
    private String externalId;

    // Номер або Індекс
    // НП: "1"
    // Укрпошта: "79000" (Поштовий індекс часто виступає номером відділення)
    @Column(nullable = false)
    private String number;

    // Опис
    // "Відділення №1: вул. Городоцька, 300"
    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    // Чиста адреса (без номеру відділення)
    @Column(columnDefinition = "TEXT")
    private String address;

    // Тип (Parcel Locker, Cargo, Post)
    // Зберігаємо як String, бо типи у всіх різні
    @Column(name = "type_description")
    private String typeDescription;

    // === JPA ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShippingBranch that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}