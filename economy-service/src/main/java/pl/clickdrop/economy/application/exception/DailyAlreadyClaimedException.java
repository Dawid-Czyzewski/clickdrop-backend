package pl.clickdrop.economy.application.exception;

import java.time.Instant;

public class DailyAlreadyClaimedException extends RuntimeException {

    private final Instant nextClaimAt;

    public DailyAlreadyClaimedException(Instant nextClaimAt) {
        super("Daily reward already claimed");
        this.nextClaimAt = nextClaimAt;
    }

    public Instant getNextClaimAt() {
        return nextClaimAt;
    }
}
