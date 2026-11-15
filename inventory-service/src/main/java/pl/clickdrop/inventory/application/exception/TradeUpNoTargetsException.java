package pl.clickdrop.inventory.application.exception;

import pl.clickdrop.inventory.domain.item.ItemRarity;

public class TradeUpNoTargetsException extends RuntimeException {
    public TradeUpNoTargetsException(ItemRarity sourceRarity) {
        super("No trade-up targets for rarity: " + sourceRarity);
    }
}
