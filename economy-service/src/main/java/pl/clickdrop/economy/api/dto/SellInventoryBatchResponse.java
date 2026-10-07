package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record SellInventoryBatchResponse(
        int soldCount,
        BigDecimal totalEarned,
        BigDecimal balance,
        List<SoldInventoryItemDto> sold,
        List<SkippedInventoryItemDto> skipped
) {
}
