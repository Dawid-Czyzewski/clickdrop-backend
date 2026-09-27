package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemRarity;

public record ItemResponse(Long id, String code, String displayName, ItemRarity rarity) {
}
