package pl.clickdrop.economy.application.exception;

public class WalletAlreadyExistsException extends RuntimeException {
    public WalletAlreadyExistsException(Long userId) {
        super("Wallet already exists for userId=" + userId);
    }
}
