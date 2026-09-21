package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record SellInventoryItemResponse(
        BigDecimal balance,
        BigDecimal soldFor
) {
}
