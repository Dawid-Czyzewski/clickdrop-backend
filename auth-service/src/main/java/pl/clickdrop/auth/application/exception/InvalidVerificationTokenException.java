package pl.clickdrop.auth.application.exception;

public class InvalidVerificationTokenException extends RuntimeException {
    public InvalidVerificationTokenException() {
        super("Invalid verification token");
    }
}
