package pl.clickdrop.inventory.api.dto;

import java.math.BigDecimal;

public record SellInventoryItemResponse(
        BigDecimal balance,
        BigDecimal soldFor
) {
}
