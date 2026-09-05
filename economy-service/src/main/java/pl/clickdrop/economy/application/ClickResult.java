package pl.clickdrop.economy.application;

import java.math.BigDecimal;

public record ClickResult(
        BigDecimal balance,
        BigDecimal earned
) {
}
