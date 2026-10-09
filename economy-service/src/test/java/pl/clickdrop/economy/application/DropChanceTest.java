package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DropChanceTest {

    @Test
    void percent_splitsPool_25_and_75() {
        assertThat(DropChance.percent(15, 60)).isEqualByComparingTo("25.00");
        assertThat(DropChance.percent(45, 60)).isEqualByComparingTo("75.00");
    }

    @Test
    void percent_singleWeight_is100() {
        assertThat(DropChance.percent(10, 10)).isEqualByComparingTo("100.00");
    }

    @Test
    void percent_zeroTotal_isZero() {
        assertThat(DropChance.percent(10, 0)).isEqualByComparingTo("0.00");
    }
}