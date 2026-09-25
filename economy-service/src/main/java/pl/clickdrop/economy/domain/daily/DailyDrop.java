package pl.clickdrop.economy.domain.daily;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "daily_drops")
public class DailyDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 16)
    private DailyRewardType rewardType;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "cash_amount", precision = 19, scale = 2)
    private BigDecimal cashAmount;

    @Column(nullable = false)
    private Integer weight;
}
