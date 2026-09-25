package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemRarity;

public record DailySkinDropResponse(
        Long itemId,
        String itemCode,
        ItemRarity rarity,
        Integer weight
) implements DailyDropResponse {
}
