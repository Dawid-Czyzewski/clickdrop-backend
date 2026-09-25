package pl.clickdrop.economy.application.exception;

public class CaseOpenRateLimitException extends RuntimeException {
    public CaseOpenRateLimitException() {
        super("Too many case open requests");
    }
}
