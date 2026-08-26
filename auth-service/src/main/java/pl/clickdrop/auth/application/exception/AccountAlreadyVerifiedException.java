package pl.clickdrop.auth.application.exception;

public class AccountAlreadyVerifiedException extends RuntimeException {
    public AccountAlreadyVerifiedException() {
        super("Account is already verified");
    }
}
