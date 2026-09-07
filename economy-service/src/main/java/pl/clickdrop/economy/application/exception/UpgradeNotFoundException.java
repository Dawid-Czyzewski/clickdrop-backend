package pl.clickdrop.economy.application.exception;

public class UpgradeNotFoundException extends RuntimeException {
    public UpgradeNotFoundException(Long upgradeId) {
        super("Upgrade not found: id=" + upgradeId);
    }
}
