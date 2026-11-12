package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemRarity;

public record CollectionItemProgressDto(
        Long itemId,
        String code,
        String displayName,
        ItemRarity rarity,
        boolean owned
) {}
