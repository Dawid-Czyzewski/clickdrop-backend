package pl.clickdrop.economy.application;

import java.math.BigDecimal;

public final class LevelUpCashReward {

    public static final BigDecimal PER_LEVEL = new BigDecimal("50.00");

    private LevelUpCashReward() {}

    public static BigDecimal forLevelGain(int previousLevel, Integer newLevel, boolean leveledUp) {
        if (!leveledUp || newLevel == null) {
            return BigDecimal.ZERO;
        }
        int gained = newLevel - previousLevel;
        if (gained <= 0) {
            return BigDecimal.ZERO;
        }
        return PER_LEVEL.multiply(BigDecimal.valueOf(gained));
    }
}
