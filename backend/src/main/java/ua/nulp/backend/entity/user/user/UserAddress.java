package ua.nulp.backend.entity.user.user;

import jakarta.persistence.*;
import lombok.*;
import ua.nulp.backend.entity.enums.DeliveryProvider;

@Entity
@Table(name = "users_addresses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    // === Географія ===
    private String region;   // "Львівська область"
    private String district; // "Стрийський район"
    @Column(nullable = false)
    private String city;     // "Миколаїв" (без району неясно, це Львівська чи Миколаївська обл)
    @Column(name = "city_id")
    private String cityId;  // Універсальний ID міста із зовнішнього API

    // === Відділення ===
    private String department;
    @Column(name = "department_id")
    private String departmentId; // Універсальний ID відділення

    // === Адресна доставка ===
    private String street;
    private String house;
    private String apartment;
    @Column(name = "street_id")
    private String streetId;

    // === Службові ===
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_provider", nullable = false)
    private DeliveryProvider deliveryProvider;

    @Column(name = "is_courier", nullable = false)
    private boolean isCourier;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    // === JPA Equals/HashCode ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAddress address)) return false;
        return id != null && id.equals(address.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}