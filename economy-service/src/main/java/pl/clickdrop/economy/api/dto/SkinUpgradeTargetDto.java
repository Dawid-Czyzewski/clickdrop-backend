package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemRarity;

import java.math.BigDecimal;

public record SkinUpgradeTargetDto(
        Long itemId,
        String itemCode,
        String itemDisplayName,
        ItemRarity rarity,
        BigDecimal suggestedSellPrice
) {}
