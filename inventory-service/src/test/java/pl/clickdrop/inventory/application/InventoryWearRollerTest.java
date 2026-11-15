package pl.clickdrop.inventory.application;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import pl.clickdrop.inventory.domain.item.ItemExterior;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryWearRollerTest {

    private final InventoryWearRoller roller = new InventoryWearRoller();

    @ParameterizedTest
    @EnumSource(ItemExterior.class)
    void roll_staysInExteriorRange(ItemExterior exterior) {
        for (int i = 0; i < 50; i++) {
            InventoryWearRoller.Wear wear = roller.roll(exterior);
            assertThat(InventoryWearRoller.isInRange(exterior, wear.floatValue())).isTrue();
            assertThat(wear.paintSeed()).isBetween(0, 999);
        }
    }
}
