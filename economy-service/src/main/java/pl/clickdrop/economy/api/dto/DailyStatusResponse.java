package pl.clickdrop.economy.api.dto;

import java.time.Instant;

public record DailyStatusResponse(
        boolean available,
        Instant nextClaimAt
) {
}
