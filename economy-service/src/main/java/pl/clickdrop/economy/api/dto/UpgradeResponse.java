package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record UpgradeResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer tier,
        BigDecimal price,
        BigDecimal clickPower,
        BigDecimal incomePerSecond
) {
}
