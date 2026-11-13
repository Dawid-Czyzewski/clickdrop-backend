package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.math.BigDecimal;

public record OpenedCaseDropResponse(
        Long inventoryItemId,
        String itemCode,
        String itemDisplayName,
        ItemRarity rarity,
        ItemExterior exterior,
        boolean statTrack,
        BigDecimal floatValue,
        Integer paintSeed,
        BigDecimal suggestedSellPrice
) {
}
