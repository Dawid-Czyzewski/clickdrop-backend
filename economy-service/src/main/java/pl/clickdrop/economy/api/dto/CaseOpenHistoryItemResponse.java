package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.time.Instant;

public record CaseOpenHistoryItemResponse(
        String itemCode,
        String displayName,
        ItemRarity rarity,
        ItemExterior exterior,
        boolean statTrack,
        Instant createdAt
) {
}
