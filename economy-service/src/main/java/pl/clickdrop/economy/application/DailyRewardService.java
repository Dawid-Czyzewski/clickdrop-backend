package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.DailyClaimResponse;
import pl.clickdrop.economy.api.dto.DailyStatusResponse;
import pl.clickdrop.economy.application.exception.CaseDropPoolEmptyException;
import pl.clickdrop.economy.application.exception.DailyAlreadyClaimedException;
import pl.clickdrop.economy.domain.daily.DailyClaim;
import pl.clickdrop.economy.domain.daily.DailyClaimRepository;
import pl.clickdrop.economy.domain.daily.DailyDrop;
import pl.clickdrop.economy.domain.daily.DailyDropRepository;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyRewardService {

    private static final Duration CLAIM_COOLDOWN = Duration.ofHours(24);

    private final DailyClaimRepository dailyClaimRepository;
    private final DailyDropRepository dailyDropRepository;
    private final ItemRepository itemRepository;
    private final UserInventoryItemRepository userInventoryItemRepository;
    private final WeightedPicker weightedPicker;
    private final ExteriorRoller exteriorRoller;

    @Transactional(readOnly = true)
    public DailyStatusResponse status(Long userId) {
        Instant now = Instant.now();

        return dailyClaimRepository.findByUserId(userId)
                .map(claim -> {
                    Instant next = claim.getClaimedAt().plus(CLAIM_COOLDOWN);
                    boolean available = !now.isBefore(next);

                    return new DailyStatusResponse(available, available ? null : next);
                })
                .orElseGet(() -> new DailyStatusResponse(true, null));
    }

    @Transactional
    public DailyClaimResponse claim(Long userId) {
        Instant now = Instant.now();

        dailyClaimRepository.findByUserId(userId).ifPresent(claim -> {
            Instant next = claim.getClaimedAt().plus(CLAIM_COOLDOWN);

            if (now.isBefore(next)) {
                throw new DailyAlreadyClaimedException(next);
            }
        });

        List<DailyDrop> pool = dailyDropRepository.findAll();
        if (pool.isEmpty()) {
            throw new CaseDropPoolEmptyException(0L);
        }

        DailyDrop chosen = weightedPicker.pick(pool, DailyDrop::getWeight);
        Item item = itemRepository.findById(chosen.getItemId())
                .filter(Item::isActive)
                .orElseThrow(() -> new CaseDropPoolEmptyException(0L));

        ItemExterior exterior = exteriorRoller.roll();

        UserInventoryItem inventoryItem = new UserInventoryItem();
        inventoryItem.setUserId(userId);
        inventoryItem.setItemId(item.getId());
        inventoryItem.setExterior(exterior);
        inventoryItem = userInventoryItemRepository.save(inventoryItem);

        DailyClaim claim = dailyClaimRepository.findByUserId(userId)
                .orElseGet(DailyClaim::new);
        claim.setUserId(userId);
        claim.setClaimedAt(now);
        dailyClaimRepository.save(claim);

        Instant nextClaimAt = now.plus(CLAIM_COOLDOWN);

        return new DailyClaimResponse(
                inventoryItem.getId(),
                item.getCode(),
                item.getRarity(),
                exterior,
                SellPricing.sellPrice(item.getRarity(), exterior),
                nextClaimAt
        );
    }
}
