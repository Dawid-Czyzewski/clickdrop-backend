package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record SoldInventoryItemDto(
        Long inventoryItemId,
        String itemCode,
        BigDecimal earned
) {
}
