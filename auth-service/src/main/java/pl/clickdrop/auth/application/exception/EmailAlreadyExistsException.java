package pl.clickdrop.auth.application.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("Email already exists");
    }
}
