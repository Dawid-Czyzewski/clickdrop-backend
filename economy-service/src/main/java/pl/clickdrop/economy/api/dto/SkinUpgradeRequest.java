package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemExterior;

public record SkinUpgradeRequest(
        Long targetItemId,
        ItemExterior targetExterior
) {}
