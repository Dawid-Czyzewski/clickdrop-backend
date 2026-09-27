package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryItemResponse(
        Long inventoryItemId,
        Long itemId,
        String code,
        String displayName,
        ItemRarity rarity,
        ItemExterior exterior,
        BigDecimal suggestedSellPrice,
        Instant createdAt
) {
}
