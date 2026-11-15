package pl.clickdrop.inventory.application.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Insufficient ClickDollars");
    }
}
