package pl.clickdrop.economy.domain.battle;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "battles")
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(nullable = false)
    private Integer rounds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BattleStatus status;

    @Column(name = "player1_user_id", nullable = false)
    private Long player1UserId;

    @Column(name = "player2_user_id")
    private Long player2UserId;

    @Column(name = "player1_total_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal player1TotalValue = BigDecimal.ZERO;

    @Column(name = "player2_total_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal player2TotalValue = BigDecimal.ZERO;

    @Column(name = "winner_user_id")
    private Long winnerUserId;

    @Column(name = "entry_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal entryCost;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (player1TotalValue == null) {
            player1TotalValue = BigDecimal.ZERO;
        }
        if (player2TotalValue == null) {
            player2TotalValue = BigDecimal.ZERO;
        }
    }
}
