package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DailyCashClaimResponse(
        BigDecimal cashAmount,
        BigDecimal balance,
        Instant nextClaimAt
) implements DailyClaimResponse {
}
