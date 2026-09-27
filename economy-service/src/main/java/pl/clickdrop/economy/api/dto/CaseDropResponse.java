package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemRarity;

public record CaseDropResponse(
        Long itemId,
        String code,
        String displayName,
        ItemRarity rarity,
        Integer weight
) {
}
