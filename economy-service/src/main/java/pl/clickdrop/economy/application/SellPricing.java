package pl.clickdrop.economy.application;

import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SellPricing {

    private SellPricing() {}

    public static BigDecimal sellPrice(ItemRarity rarity, ItemExterior exterior) {
        BigDecimal base = switch (rarity) {
            case MIL_SPEC -> new BigDecimal("15");
            case RESTRICTED -> new BigDecimal("40");
            case CLASSIFIED -> new BigDecimal("100");
            case COVERT -> new BigDecimal("250");
        };

        BigDecimal multiplier = switch (exterior) {
            case FACTORY_NEW -> new BigDecimal("1.40");
            case MINIMAL_WEAR -> new BigDecimal("1.20");
            case FIELD_TESTED -> new BigDecimal("1.00");
            case WELL_WORN -> new BigDecimal("0.75");
            case BATTLE_SCARRED -> new BigDecimal("0.50");
        };

        return base.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
