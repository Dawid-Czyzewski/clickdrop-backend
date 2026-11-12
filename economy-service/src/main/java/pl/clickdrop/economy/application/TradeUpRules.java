package pl.clickdrop.economy.application;

import pl.clickdrop.economy.application.exception.TradeUpInvalidException;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public final class TradeUpRules {

    public static final int REQUIRED_COUNT = 10;

    private static final ItemExterior[] EXTERIORS = ItemExterior.values();

    private TradeUpRules() {}

    public static List<Long> requireExactUniqueIds(List<Long> raw) {
        if (raw == null || raw.size() != REQUIRED_COUNT || raw.stream().anyMatch(Objects::isNull)) {
            throw new TradeUpInvalidException(
                    "Trade-up requires exactly " + REQUIRED_COUNT + " inventory item ids");
        }
        List<Long> unique = List.copyOf(new LinkedHashSet<>(raw));
        if (unique.size() != REQUIRED_COUNT) {
            throw new TradeUpInvalidException(
                    "Trade-up requires exactly " + REQUIRED_COUNT + " unique inventory item ids");
        }
        return unique;
    }

    public static ItemRarity nextRarity(ItemRarity rarity) {
        return switch (rarity) {
            case MIL_SPEC -> ItemRarity.RESTRICTED;
            case RESTRICTED -> ItemRarity.CLASSIFIED;
            case CLASSIFIED -> ItemRarity.COVERT;
            case COVERT -> null;
        };
    }

    public static ItemExterior averageExterior(List<ItemExterior> exteriors) {
        if (exteriors == null || exteriors.size() != REQUIRED_COUNT
                || exteriors.stream().anyMatch(Objects::isNull)) {
            throw new TradeUpInvalidException("Trade-up exterior average requires 10 exteriors");
        }
        double avg = exteriors.stream().mapToInt(Enum::ordinal).average().orElseThrow();
        int index = (int) Math.round(avg);
        if (index < 0) {
            index = 0;
        } else if (index >= EXTERIORS.length) {
            index = EXTERIORS.length - 1;
        }
        return EXTERIORS[index];
    }

    public static boolean resultStatTrack(List<Boolean> flags) {
        if (flags == null || flags.size() != REQUIRED_COUNT) {
            throw new TradeUpInvalidException("Trade-up StatTrak requires 10 flags");
        }
        return flags.stream().allMatch(Boolean.TRUE::equals);
    }
}
