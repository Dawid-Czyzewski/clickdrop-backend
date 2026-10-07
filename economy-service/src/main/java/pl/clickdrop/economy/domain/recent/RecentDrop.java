package pl.clickdrop.economy.domain.recent;

import jakarta.persistence.*;
import lombok.Data;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.time.Instant;

@Data
@Entity
@Table(name = "recent_drops")
public class RecentDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_code", nullable = false, length = 64)
    private String itemCode;

    @Column(name = "item_display_name", nullable = false, length = 128)
    private String itemDisplayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ItemRarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ItemExterior exterior;

    @Column(name = "stat_track", nullable = false)
    private boolean statTrack;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RecentDropSource source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
