package pl.clickdrop.inventory.application.exception;

public class InvalidSellBatchException extends RuntimeException {
    public InvalidSellBatchException() {
        super("Invalid sell batch request");
    }
}
