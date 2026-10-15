package pl.clickdrop.auth.application.exception;

public class ExpiredPasswordResetTokenException extends RuntimeException {
    public ExpiredPasswordResetTokenException() {
        super("Password reset token expired");
    }
}
