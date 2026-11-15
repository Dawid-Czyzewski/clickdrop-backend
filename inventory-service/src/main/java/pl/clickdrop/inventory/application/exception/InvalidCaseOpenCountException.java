package pl.clickdrop.inventory.application.exception;

public class InvalidCaseOpenCountException extends RuntimeException {
    public InvalidCaseOpenCountException() {
        super("Invalid case open count");
    }
}
