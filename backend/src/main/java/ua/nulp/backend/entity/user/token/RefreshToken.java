package ua.nulp.backend.entity.user.token;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ua.nulp.backend.entity.user.user.User;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(nullable = false, updatable = false, name = "expired_at")
    private Instant expiredAt;

    // === JPA Equals/HashCode ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken refreshToken)) return false;
        return id != null && id.equals(refreshToken.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}