package pl.clickdrop.economy.api.dto;

public record SkippedInventoryItemDto(
        Long inventoryItemId,
        SellSkipReason reason
) {
}
