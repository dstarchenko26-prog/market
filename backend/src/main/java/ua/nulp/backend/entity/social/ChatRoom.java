package ua.nulp.backend.entity.social;

import jakarta.persistence.*;
import lombok.*;
import ua.nulp.backend.entity.shop.Shop;
import ua.nulp.backend.entity.user.user.User;

import java.time.Instant;

@Entity
@Table(name = "chat_rooms", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_buyer_shop", columnNames = {"buyer_id", "shop_id"})
})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    @ToString.Exclude
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @ToString.Exclude
    private Shop shop;

    @Column(name = "last_message_at", nullable = false)
    @Builder.Default
    private Instant lastMessageAt = Instant.now();

    // === JPA Equals/HashCode ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoom chatRoom)) return false;
        return id != null && id.equals(chatRoom.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}