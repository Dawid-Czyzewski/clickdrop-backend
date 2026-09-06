package pl.clickdrop.economy.domain.upgrade;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(
        name = "user_upgrades",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_upgrades_user_upgrade",
                        columnNames = {"user_id", "upgrade_id"}
                )
        }
)
public class UserUpgrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "upgrade_id", nullable = false)
    private Long upgradeId;

    @Column(nullable = false)
    private Integer level = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upgrade_id", insertable = false, updatable = false)
    private Upgrade upgrade;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (level == null) {
            level = 0;
        }
    }
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
