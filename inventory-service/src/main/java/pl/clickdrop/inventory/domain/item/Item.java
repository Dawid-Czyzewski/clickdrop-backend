package pl.clickdrop.inventory.domain.item;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128, unique = true)
    private String code;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ItemRarity rarity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
