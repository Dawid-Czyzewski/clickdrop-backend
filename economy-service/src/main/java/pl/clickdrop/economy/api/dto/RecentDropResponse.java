package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.recent.RecentDropSource;

import java.time.Instant;

public record RecentDropResponse(
        String itemCode,
        String displayName,
        ItemRarity rarity,
        ItemExterior exterior,
        boolean statTrack,
        RecentDropSource source,
        Instant createdAt
) {
}
