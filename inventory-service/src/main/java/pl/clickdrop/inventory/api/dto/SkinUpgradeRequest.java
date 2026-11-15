package pl.clickdrop.inventory.api.dto;

import pl.clickdrop.inventory.domain.item.ItemExterior;

public record SkinUpgradeRequest(
        Long targetItemId,
        ItemExterior targetExterior
) {}
