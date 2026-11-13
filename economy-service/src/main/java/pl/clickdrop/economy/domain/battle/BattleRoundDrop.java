package pl.clickdrop.economy.domain.battle;

import jakarta.persistence.*;
import lombok.Data;
import pl.clickdrop.economy.domain.item.ItemExterior;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "battle_round_drops")
public class BattleRoundDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "battle_id", nullable = false)
    private Long battleId;

    @Column(name = "round_index", nullable = false)
    private Integer roundIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BattleSide side;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ItemExterior exterior;

    @Column(name = "stat_track", nullable = false)
    private boolean statTrack;

    @Column(name = "drop_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal dropValue;

    @Column(name = "inventory_item_id")
    private Long inventoryItemId;
}
