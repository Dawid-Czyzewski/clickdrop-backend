package pl.clickdrop.economy.application.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Insufficient ClickDollars");
    }
}
