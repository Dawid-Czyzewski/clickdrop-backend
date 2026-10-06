package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.math.BigDecimal;
import java.time.Instant;

public record DailySkinClaimResponse(
        Long inventoryItemId,
        String itemCode,
        String itemDisplayName,
        ItemRarity rarity,
        ItemExterior exterior,
        boolean statTrack,
        BigDecimal suggestedSellPrice,
        Instant nextClaimAt
) implements DailyClaimResponse {
}
