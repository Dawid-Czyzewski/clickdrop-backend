package pl.clickdrop.inventory.api.dto;

import pl.clickdrop.inventory.domain.item.ItemExterior;
import pl.clickdrop.inventory.domain.item.ItemRarity;

import java.math.BigDecimal;

public record TradeUpResponse(
        Long inventoryItemId,
        Long itemId,
        String code,
        String displayName,
        ItemRarity rarity,
        ItemExterior exterior,
        boolean statTrack,
        BigDecimal floatValue,
        Integer paintSeed,
        BigDecimal suggestedSellPrice
) {}
