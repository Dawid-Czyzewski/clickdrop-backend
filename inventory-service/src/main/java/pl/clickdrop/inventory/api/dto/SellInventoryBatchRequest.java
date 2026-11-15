package pl.clickdrop.inventory.api.dto;

import java.util.List;

public record SellInventoryBatchRequest(
        List<Long> inventoryItemIds
) {
}
