package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.math.BigDecimal;

public record OpenCaseResponse(
        Long inventoryItemId,
        String itemCode,
        String itemDisplayName,
        ItemRarity rarity,
        ItemExterior exterior,
        BigDecimal balance,
        BigDecimal cost,
        BigDecimal suggestedSellPrice
) {
}
