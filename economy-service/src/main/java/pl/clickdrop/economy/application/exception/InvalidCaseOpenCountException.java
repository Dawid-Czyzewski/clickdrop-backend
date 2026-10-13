package pl.clickdrop.economy.application.exception;

public class InvalidCaseOpenCountException extends RuntimeException {
    public InvalidCaseOpenCountException() {
        super("Invalid case open count");
    }
}
