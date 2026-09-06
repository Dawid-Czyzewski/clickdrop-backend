package pl.clickdrop.economy.application.exception;

public class ClickRateLimitException extends RuntimeException {
    public ClickRateLimitException() {
        super("Too many click requests");
    }
}
