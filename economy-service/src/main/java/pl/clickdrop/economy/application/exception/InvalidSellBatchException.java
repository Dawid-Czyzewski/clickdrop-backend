package pl.clickdrop.economy.application.exception;

public class InvalidSellBatchException extends RuntimeException {
    public InvalidSellBatchException() {
        super("Invalid sell batch request");
    }
}
