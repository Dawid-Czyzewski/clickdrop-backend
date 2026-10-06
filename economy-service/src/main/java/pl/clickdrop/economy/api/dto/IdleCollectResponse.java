package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record IdleCollectResponse(
        BigDecimal earned,
        BigDecimal balance,
        BigDecimal incomePerSecond
) {
}
