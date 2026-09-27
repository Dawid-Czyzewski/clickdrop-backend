package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.math.BigDecimal;

public record SkinUpgradeCurrentDto(
        String itemCode,
        ItemRarity rarity,
        ItemExterior exterior,
        BigDecimal suggestedSellPrice
) {}
