package pl.clickdrop.inventory.application.exception;

public class UpgradeNotFoundException extends RuntimeException {
    public UpgradeNotFoundException(Long upgradeId) {
        super("Upgrade not found: id=" + upgradeId);
    }
}
