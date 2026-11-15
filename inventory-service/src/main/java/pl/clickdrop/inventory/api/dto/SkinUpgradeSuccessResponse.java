package pl.clickdrop.inventory.api.dto;

import pl.clickdrop.inventory.domain.item.ItemExterior;
import pl.clickdrop.inventory.domain.item.ItemRarity;

import java.math.BigDecimal;

public record SkinUpgradeSuccessResponse(
        Long inventoryItemId,
        String itemCode,
        String itemDisplayName,
        ItemRarity rarity,
        ItemExterior exterior,
        boolean statTrack,
        BigDecimal floatValue,
        Integer paintSeed,
        BigDecimal suggestedSellPrice
) implements SkinUpgradeResponse {}
