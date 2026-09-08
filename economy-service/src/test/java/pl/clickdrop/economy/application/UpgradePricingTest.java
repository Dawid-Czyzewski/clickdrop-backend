package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class UpgradePricingTest {

    @Test
    void nextCost_level0_isBasePrice() {
        assertThat(UpgradePricing.nextCost(new BigDecimal("50"), 0))
                .isEqualByComparingTo("50");
    }

    @Test
    void nextCost_level1_isDouble() {
        assertThat(UpgradePricing.nextCost(new BigDecimal("50"), 1))
                .isEqualByComparingTo("100");
    }

    @Test
    void nextCost_level2_isTriple() {
        assertThat(UpgradePricing.nextCost(new BigDecimal("50"), 2))
                .isEqualByComparingTo("150");
    }
}