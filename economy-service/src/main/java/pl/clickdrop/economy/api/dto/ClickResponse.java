package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record ClickResponse(
        BigDecimal balance,
        BigDecimal earned
) {
}
