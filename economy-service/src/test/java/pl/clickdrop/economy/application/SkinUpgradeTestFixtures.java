package pl.clickdrop.economy.application;

import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

final class SkinUpgradeTestFixtures {

    private SkinUpgradeTestFixtures() {}

    static UserInventoryItem ownedRow(
            Long id, Long userId, Long itemId, ItemExterior exterior, boolean statTrack
    ) {
        UserInventoryItem row = new UserInventoryItem();
        row.setId(id);
        row.setUserId(userId);
        row.setItemId(itemId);
        row.setExterior(exterior);
        row.setStatTrack(statTrack);
        return row;
    }

    static Item item(Long id, String code, ItemRarity rarity) {
        Item item = new Item();
        item.setId(id);
        item.setCode(code);
        item.setDisplayName(code.replace('_', ' '));
        item.setRarity(rarity);
        item.setActive(true);
        return item;
    }
}
