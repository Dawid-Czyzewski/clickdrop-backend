package pl.clickdrop.economy.application;

import pl.clickdrop.economy.domain.daily.DailyClaim;
import pl.clickdrop.economy.domain.daily.DailyDrop;
import pl.clickdrop.economy.domain.daily.DailyRewardType;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.wallet.Wallet;

import java.math.BigDecimal;
import java.time.Instant;

final class DailyRewardTestFixtures {

    private DailyRewardTestFixtures() {}

    static DailyClaim claim(Long userId, Instant claimedAt) {
        DailyClaim claim = new DailyClaim();
        claim.setUserId(userId);
        claim.setClaimedAt(claimedAt);
        return claim;
    }

    static DailyDrop skinDrop(Long id, Long itemId, int weight) {
        DailyDrop drop = new DailyDrop();
        drop.setId(id);
        drop.setRewardType(DailyRewardType.SKIN);
        drop.setItemId(itemId);
        drop.setWeight(weight);
        return drop;
    }

    static DailyDrop cashDrop(Long id, String amount, int weight) {
        DailyDrop drop = new DailyDrop();
        drop.setId(id);
        drop.setRewardType(DailyRewardType.CASH);
        drop.setCashAmount(new BigDecimal(amount));
        drop.setWeight(weight);
        return drop;
    }

    static Wallet wallet(Long userId, String balance) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal(balance));
        return wallet;
    }

    static Item item(Long id, String code, ItemRarity rarity) {
        Item item = new Item();
        item.setId(id);
        item.setCode(code);
        item.setRarity(rarity);
        item.setActive(true);
        return item;
    }
}
