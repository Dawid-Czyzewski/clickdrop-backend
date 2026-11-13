package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

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
