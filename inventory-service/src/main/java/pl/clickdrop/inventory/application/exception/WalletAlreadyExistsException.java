package pl.clickdrop.inventory.application.exception;

public class WalletAlreadyExistsException extends RuntimeException {
    public WalletAlreadyExistsException(Long userId) {
        super("Wallet already exists for userId=" + userId);
    }
}
