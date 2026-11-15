package pl.clickdrop.inventory.api.dto;

import java.util.List;

public record SkinUpgradePreviewResponse(
        Long inventoryItemId,
        SkinUpgradeCurrentDto current,
        double successChance,
        List<SkinUpgradeTargetDto> possibleTargets
) {}
