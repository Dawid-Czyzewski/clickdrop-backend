package pl.clickdrop.inventory.application.exception;

public class ClickRateLimitException extends RuntimeException {
    public ClickRateLimitException() {
        super("Too many click requests");
    }
}
