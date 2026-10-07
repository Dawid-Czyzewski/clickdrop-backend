package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.DailyCashClaimResponse;
import pl.clickdrop.economy.api.dto.DailyCashDropResponse;
import pl.clickdrop.economy.api.dto.DailyClaimResponse;
import pl.clickdrop.economy.api.dto.DailyDropResponse;
import pl.clickdrop.economy.api.dto.DailySkinClaimResponse;
import pl.clickdrop.economy.api.dto.DailySkinDropResponse;
import pl.clickdrop.economy.api.dto.DailyStatusResponse;
import pl.clickdrop.economy.application.exception.CaseDropPoolEmptyException;
import pl.clickdrop.economy.application.exception.DailyAlreadyClaimedException;
import pl.clickdrop.economy.application.exception.WalletNotFoundException;
import pl.clickdrop.economy.domain.daily.DailyClaim;
import pl.clickdrop.economy.domain.daily.DailyClaimRepository;
import pl.clickdrop.economy.domain.daily.DailyDrop;
import pl.clickdrop.economy.domain.daily.DailyDropRepository;
import pl.clickdrop.economy.domain.daily.DailyRewardType;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.recent.RecentDropSource;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyRewardService {

    private static final Duration CLAIM_COOLDOWN = Duration.ofHours(24);

    private final DailyClaimRepository dailyClaimRepository;
    private final DailyDropRepository dailyDropRepository;
    private final ItemRepository itemRepository;
    private final UserInventoryItemRepository userInventoryItemRepository;
    private final WalletRepository walletRepository;
    private final WeightedPicker weightedPicker;
    private final ExteriorRoller exteriorRoller;
    private final StatTrackRoller StatTrackRoller;
    private final RecentDropRecorder recentDropRecorder;

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

    @Transactional(readOnly = true)
    public List<DailyDropResponse> listDrops() {
        List<DailyDrop> drops = dailyDropRepository.findAll();
        if (drops.isEmpty()) {
            return List.of();
        }

        Map<Long, Item> itemsById = itemRepository.findAllById(
                drops.stream().map(DailyDrop::getItemId).filter(Objects::nonNull).toList()
        ).stream().collect(Collectors.toMap(Item::getId, Function.identity()));

        return drops.stream()
                .map(drop -> toDropResponse(drop, itemsById))
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public DailyClaimResponse claim(Long userId) {
        Instant now = Instant.now();
        assertCooldownElapsed(userId, now);

        List<DailyDrop> pool = dailyDropRepository.findAll();
        if (pool.isEmpty()) {
            throw new CaseDropPoolEmptyException(0L);
        }

        DailyDrop chosen = weightedPicker.pick(pool, DailyDrop::getWeight);
        Instant nextClaimAt = now.plus(CLAIM_COOLDOWN);

        DailyClaimResponse response = switch (chosen.getRewardType()) {
            case SKIN -> claimSkin(userId, chosen, nextClaimAt);
            case CASH -> claimCash(userId, chosen, nextClaimAt);
        };

        saveClaim(userId, now);
        return response;
    }

    private void assertCooldownElapsed(Long userId, Instant now) {
        dailyClaimRepository.findByUserId(userId).ifPresent(claim -> {
            Instant next = claim.getClaimedAt().plus(CLAIM_COOLDOWN);
            if (now.isBefore(next)) {
                throw new DailyAlreadyClaimedException(next);
            }
        });
    }

    private DailyClaimResponse claimSkin(Long userId, DailyDrop chosen, Instant nextClaimAt) {
        Item item = itemRepository.findById(chosen.getItemId())
                .filter(Item::isActive)
                .orElseThrow(() -> new CaseDropPoolEmptyException(0L));

        ItemExterior exterior = exteriorRoller.roll();
        boolean statTrack = StatTrackRoller.roll();

        UserInventoryItem inventoryItem = new UserInventoryItem();
        inventoryItem.setUserId(userId);
        inventoryItem.setItemId(item.getId());
        inventoryItem.setExterior(exterior);
        inventoryItem.setStatTrack(statTrack);
        inventoryItem = userInventoryItemRepository.save(inventoryItem);

        recentDropRecorder.record(userId, item, exterior, statTrack, RecentDropSource.CASE);

        return new DailySkinClaimResponse(
                inventoryItem.getId(),
                item.getCode(),
                item.getDisplayName(),
                item.getRarity(),
                exterior,
                statTrack,
                SellPricing.sellPrice(item.getRarity(), exterior, statTrack),
                nextClaimAt
        );
    }

    private DailyClaimResponse claimCash(Long userId, DailyDrop chosen, Instant nextClaimAt) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        BigDecimal amount = chosen.getCashAmount();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        return new DailyCashClaimResponse(amount, wallet.getBalance(), nextClaimAt);
    }

    private void saveClaim(Long userId, Instant now) {
        DailyClaim claim = dailyClaimRepository.findByUserId(userId).orElseGet(DailyClaim::new);
        claim.setUserId(userId);
        claim.setClaimedAt(now);
        dailyClaimRepository.save(claim);
    }

    private static DailyDropResponse toDropResponse(DailyDrop drop, Map<Long, Item> itemsById) {
        if (drop.getRewardType() == DailyRewardType.CASH) {
            return new DailyCashDropResponse(drop.getCashAmount(), drop.getWeight());
        }

        Item item = itemsById.get(drop.getItemId());
        if (item == null || !item.isActive()) {
            return null;
        }

        return new DailySkinDropResponse(
                item.getId(),
                item.getCode(),
                item.getDisplayName(),
                item.getRarity(),
                drop.getWeight()
        );
    }
}
