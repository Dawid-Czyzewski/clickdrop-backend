package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record MyUpgradeResponse(
        Long id,
        String code,
        Integer tier,
        BigDecimal price,
        BigDecimal clickPower,
        Integer level,
        BigDecimal nextCost,
        BigDecimal incomePerSecond
) {
}
