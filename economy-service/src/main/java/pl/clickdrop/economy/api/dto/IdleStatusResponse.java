package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record IdleStatusResponse(
        BigDecimal pending,
        BigDecimal incomePerSecond,
        Instant lastIdleCollectedAt
) {
}
