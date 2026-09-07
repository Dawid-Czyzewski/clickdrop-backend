package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record BuyUpgradeResponse(
        Long upgradeId,
        String code,
        Integer level,
        BigDecimal balance,
        BigDecimal cost
) {
}
