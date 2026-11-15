package pl.clickdrop.inventory.application.exception;

public class InventoryItemNotFoundException extends RuntimeException {
    public InventoryItemNotFoundException(Long inventoryItemId) {
        super("Inventory item not found: id=" + inventoryItemId);
    }
}
