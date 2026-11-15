package pl.clickdrop.inventory.api.dto;

public record SkippedInventoryItemDto(
        Long inventoryItemId,
        SellSkipReason reason
) {
}
