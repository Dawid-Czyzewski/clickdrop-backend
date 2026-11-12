package pl.clickdrop.economy.api.dto;

import java.util.List;

public record TradeUpRequest(
        List<Long> inventoryItemIds
) {}
