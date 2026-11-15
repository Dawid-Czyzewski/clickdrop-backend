package pl.clickdrop.inventory.application;

import org.springframework.stereotype.Component;
import pl.clickdrop.inventory.domain.item.ItemExterior;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class InventoryWearRoller {

    public record Wear(BigDecimal floatValue, int paintSeed) {}

    public Wear roll(ItemExterior exterior) {
        return new Wear(rollFloat(exterior), rollPaintSeed());
    }

    public BigDecimal rollFloat(ItemExterior exterior) {
        double[] range = rangeFor(exterior);
        double value = ThreadLocalRandom.current().nextDouble(range[0], range[1]);
        BigDecimal scaled = BigDecimal.valueOf(value).setScale(6, RoundingMode.DOWN);
        if (scaled.doubleValue() < range[0]) {
            return BigDecimal.valueOf(range[0]).setScale(6, RoundingMode.DOWN);
        }
        return scaled;
    }

    public int rollPaintSeed() {
        return ThreadLocalRandom.current().nextInt(0, 1000);
    }

    static double[] rangeFor(ItemExterior exterior) {
        return switch (exterior) {
            case FACTORY_NEW -> new double[] { 0.00, 0.07 };
            case MINIMAL_WEAR -> new double[] { 0.07, 0.15 };
            case FIELD_TESTED -> new double[] { 0.15, 0.38 };
            case WELL_WORN -> new double[] { 0.38, 0.45 };
            case BATTLE_SCARRED -> new double[] { 0.45, 1.00 };
        };
    }

    public static boolean isInRange(ItemExterior exterior, BigDecimal floatValue) {
        if (floatValue == null) return false;
        double[] range = rangeFor(exterior);
        double v = floatValue.doubleValue();
        return v >= range[0] && v < range[1];
    }
}
