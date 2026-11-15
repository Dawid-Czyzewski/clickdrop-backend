package pl.clickdrop.inventory.application.exception;

public class CaseOpenRateLimitException extends RuntimeException {
    public CaseOpenRateLimitException() {
        super("Too many case open requests");
    }
}
