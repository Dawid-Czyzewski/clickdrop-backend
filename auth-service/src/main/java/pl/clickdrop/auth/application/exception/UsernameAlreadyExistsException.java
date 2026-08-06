package pl.clickdrop.auth.application.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException() {
        super("Username already exists");
    }
}
