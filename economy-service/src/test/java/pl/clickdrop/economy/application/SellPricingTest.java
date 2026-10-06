package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import static org.assertj.core.api.Assertions.assertThat;

class SellPricingTest {

    @Test
    void milSpec_fieldTested_isBasePrice() {
        assertThat(SellPricing.sellPrice(ItemRarity.MIL_SPEC, ItemExterior.FIELD_TESTED))
                .isEqualByComparingTo("15.00");
    }

    @Test
    void covert_factoryNew_isHigherThanBattleScarred() {
        var fn = SellPricing.sellPrice(ItemRarity.COVERT, ItemExterior.FACTORY_NEW);
        var bs = SellPricing.sellPrice(ItemRarity.COVERT, ItemExterior.BATTLE_SCARRED);

        assertThat(fn).isEqualByComparingTo("350.00");
        assertThat(bs).isEqualByComparingTo("125.00");
        assertThat(fn).isGreaterThan(bs);
    }

    @Test
    void restricted_minimalWear() {
        assertThat(SellPricing.sellPrice(ItemRarity.RESTRICTED, ItemExterior.MINIMAL_WEAR))
                .isEqualByComparingTo("48.00");
    }

    @Test
    void milSpec_fieldTested_statTrack_isBaseTimes125() {
        assertThat(SellPricing.sellPrice(ItemRarity.MIL_SPEC, ItemExterior.FIELD_TESTED, true))
                .isEqualByComparingTo("18.75");
    }

    @Test
    void twoArgOverload_matchesNonStatTrack() {
        assertThat(SellPricing.sellPrice(ItemRarity.RESTRICTED, ItemExterior.FIELD_TESTED))
                .isEqualByComparingTo(
                        SellPricing.sellPrice(ItemRarity.RESTRICTED, ItemExterior.FIELD_TESTED, false));
    }
}
