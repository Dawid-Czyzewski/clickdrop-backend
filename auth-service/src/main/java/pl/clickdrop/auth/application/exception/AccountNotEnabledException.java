package pl.clickdrop.auth.application.exception;

public class AccountNotEnabledException extends RuntimeException {
    public AccountNotEnabledException() {
        super("Account is not verified. Please confirm your email.");
    }
}
