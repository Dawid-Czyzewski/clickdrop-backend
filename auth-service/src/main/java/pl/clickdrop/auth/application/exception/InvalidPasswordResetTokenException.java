package pl.clickdrop.auth.application.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {
    public InvalidPasswordResetTokenException() {
        super("Invalid password reset token");
    }
}
