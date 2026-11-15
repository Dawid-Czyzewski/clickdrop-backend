package pl.clickdrop.inventory.application.exception;

public class SkinUpgradeInvalidTargetException extends RuntimeException {

    public SkinUpgradeInvalidTargetException(Long inventoryItemId) {
        super("Invalid skin upgrade target for inventory item " + inventoryItemId);
    }
}
