package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record DailyCashDropResponse(
        BigDecimal cashAmount,
        Integer weight
) implements DailyDropResponse {
}
