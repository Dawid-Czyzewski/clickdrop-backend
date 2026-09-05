package pl.clickdrop.economy.application.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(Long userId) {
        super("Wallet not found for userId=" + userId);
    }
}