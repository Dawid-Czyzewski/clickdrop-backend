package pl.clickdrop.economy.domain.inventory;

import jakarta.persistence.*;
import lombok.Data;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "user_inventory_items")
public class UserInventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ItemExterior exterior;

    @Column(name = "stat_track", nullable = false)
    private boolean statTrack = false;

    @Column(name = "float_value", nullable = false, precision = 8, scale = 6)
    private BigDecimal floatValue;

    @Column(name = "paint_seed", nullable = false)
    private Integer paintSeed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
