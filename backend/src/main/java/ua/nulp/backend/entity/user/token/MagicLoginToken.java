package ua.nulp.backend.entity.user.token;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ua.nulp.backend.entity.user.user.User;

import java.time.Instant;

@Entity
@Table(name = "magic_login_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MagicLoginToken {
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

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(nullable = false, updatable = false, name = "expired_at")
    private Instant expiredAt;

    public boolean isValid() {
        return usedAt == null && expiredAt.isAfter(Instant.now());
    }

    // === JPA Equals/HashCode ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MagicLoginToken ml_token)) return false;
        return id != null && id.equals(ml_token.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}