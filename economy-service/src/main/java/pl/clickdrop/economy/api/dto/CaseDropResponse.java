package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemRarity;

import java.math.BigDecimal;

public record CaseDropResponse(
        Long itemId,
        String code,
        String displayName,
        ItemRarity rarity,
        Integer weight,
        BigDecimal chancePercent
) {
}
