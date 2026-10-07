package pl.clickdrop.economy.api.dto;

import java.util.List;

public record SellInventoryBatchRequest(
        List<Long> inventoryItemIds
) {
}
