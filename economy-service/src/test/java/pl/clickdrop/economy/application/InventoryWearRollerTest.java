package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import pl.clickdrop.economy.domain.item.ItemExterior;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryWearRollerTest {

    private final InventoryWearRoller roller = new InventoryWearRoller();

    @ParameterizedTest
    @EnumSource(ItemExterior.class)
    void roll_floatStaysInExteriorRange(ItemExterior exterior) {
        for (int i = 0; i < 200; i++) {
            InventoryWearRoller.Wear wear = roller.roll(exterior);
            assertThat(InventoryWearRoller.isInRange(exterior, wear.floatValue()))
                    .as("%s float %s", exterior, wear.floatValue())
                    .isTrue();
            assertThat(wear.floatValue().scale()).isEqualTo(6);
            assertThat(wear.paintSeed()).isBetween(0, 999);
        }
    }

    @Test
    void rangeFor_matchesCsWearBands() {
        assertThat(InventoryWearRoller.rangeFor(ItemExterior.FACTORY_NEW))
                .containsExactly(0.00, 0.07);
        assertThat(InventoryWearRoller.rangeFor(ItemExterior.MINIMAL_WEAR))
                .containsExactly(0.07, 0.15);
        assertThat(InventoryWearRoller.rangeFor(ItemExterior.FIELD_TESTED))
                .containsExactly(0.15, 0.38);
        assertThat(InventoryWearRoller.rangeFor(ItemExterior.WELL_WORN))
                .containsExactly(0.38, 0.45);
        assertThat(InventoryWearRoller.rangeFor(ItemExterior.BATTLE_SCARRED))
                .containsExactly(0.45, 1.00);
    }

    @Test
    void isInRange_rejectsOutsideBand() {
        assertThat(InventoryWearRoller.isInRange(
                ItemExterior.FACTORY_NEW, new BigDecimal("0.070000"))).isFalse();
        assertThat(InventoryWearRoller.isInRange(
                ItemExterior.FIELD_TESTED, new BigDecimal("0.250000"))).isTrue();
    }
}
