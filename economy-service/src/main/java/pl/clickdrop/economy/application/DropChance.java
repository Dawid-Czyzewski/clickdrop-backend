package pl.clickdrop.economy.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DropChance {

    private DropChance() {}

    public static BigDecimal percent(int weight, int totalWeight) {
        if (totalWeight <= 0 || weight <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(weight)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);
    }
}
