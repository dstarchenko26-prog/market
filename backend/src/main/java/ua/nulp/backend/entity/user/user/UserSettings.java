package ua.nulp.backend.entity.user.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users_settings")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    private String language = "UK";

    @Builder.Default
    private String theme = "LIGHT";

    @Column(name = "email_notification")
    @Builder.Default
    private boolean emailNotification = true;

    @Column(name = "phone_notification")
    @Builder.Default
    private boolean phoneNotification = false;

    // === JPA Equals/HashCode ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSettings settings)) return false;
        return userId != null && userId.equals(settings.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}