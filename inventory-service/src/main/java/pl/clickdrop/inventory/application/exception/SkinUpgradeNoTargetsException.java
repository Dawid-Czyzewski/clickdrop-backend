package pl.clickdrop.inventory.application.exception;

public class SkinUpgradeNoTargetsException extends RuntimeException {
    public SkinUpgradeNoTargetsException(Long inventoryItemId) {
        super("No more expensive upgrade targets for inventoryItemId=" + inventoryItemId);
    }
}
