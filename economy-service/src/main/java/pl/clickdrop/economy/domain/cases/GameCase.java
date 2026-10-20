package pl.clickdrop.economy.domain.cases;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "cases")
public class GameCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128, unique = true)
    private String code;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName = "";

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "min_level", nullable = false)
    private Integer minLevel = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
