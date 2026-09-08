package pl.clickdrop.economy.application;

import java.math.BigDecimal;

public final class UpgradePricing {

    private UpgradePricing() {

    }

    public static BigDecimal nextCost(BigDecimal basePrice, int currentLevel) {
        if (basePrice == null) {
            throw new IllegalArgumentException("basePrice is required");
        }

        if (currentLevel < 0) {
            throw new IllegalArgumentException("currentLevel must be >= 0");
        }

        return basePrice.multiply(BigDecimal.valueOf(currentLevel + 1L));
    }
}
