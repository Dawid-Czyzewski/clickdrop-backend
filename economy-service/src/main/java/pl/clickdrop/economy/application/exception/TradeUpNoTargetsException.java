package pl.clickdrop.economy.application.exception;

import pl.clickdrop.economy.domain.item.ItemRarity;

public class TradeUpNoTargetsException extends RuntimeException {
    public TradeUpNoTargetsException(ItemRarity sourceRarity) {
        super("No trade-up targets for rarity: " + sourceRarity);
    }
}
