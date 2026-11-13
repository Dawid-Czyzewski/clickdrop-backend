package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.battle.BattleSide;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.math.BigDecimal;

public record BattleDropDto(
        int roundIndex,
        BattleSide side,
        Long itemId,
        String itemCode,
        String itemDisplayName,
        ItemRarity rarity,
        ItemExterior exterior,
        boolean statTrack,
        BigDecimal value,
        Long inventoryItemId
) {}
