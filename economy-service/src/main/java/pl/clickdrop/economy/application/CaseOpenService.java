package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.OpenCasesResponse;
import pl.clickdrop.economy.api.dto.OpenedCaseDropResponse;
import pl.clickdrop.economy.application.client.AuthClient;
import pl.clickdrop.economy.application.client.AuthExperienceResponse;
import pl.clickdrop.economy.application.exception.CaseDropPoolEmptyException;
import pl.clickdrop.economy.application.exception.CaseLevelTooLowException;
import pl.clickdrop.economy.application.exception.CaseNotFoundException;
import pl.clickdrop.economy.application.exception.CaseOpenCountLevelTooLowException;
import pl.clickdrop.economy.application.exception.InsufficientFundsException;
import pl.clickdrop.economy.application.exception.InvalidCaseOpenCountException;
import pl.clickdrop.economy.application.exception.WalletNotFoundException;
import pl.clickdrop.economy.domain.cases.CaseDrop;
import pl.clickdrop.economy.domain.cases.CaseDropRepository;
import pl.clickdrop.economy.domain.cases.GameCase;
import pl.clickdrop.economy.domain.cases.GameCaseRepository;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.recent.RecentDropSource;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseOpenService {

    static final int MIN_OPEN_COUNT = CaseOpenCountPolicy.MIN_OPEN_COUNT;
    static final int MAX_OPEN_COUNT = CaseOpenCountPolicy.MAX_OPEN_COUNT;
    static final int OPEN_X3_MIN_LEVEL = CaseOpenCountPolicy.OPEN_X3_MIN_LEVEL;
    static final int OPEN_X5_MIN_LEVEL = CaseOpenCountPolicy.OPEN_X5_MIN_LEVEL;
    private static final int CASE_OPEN_XP = 10;

    private final GameCaseRepository gameCaseRepository;
    private final CaseDropRepository caseDropRepository;
    private final ItemRepository itemRepository;
    private final WalletRepository walletRepository;
    private final UserInventoryItemRepository inventoryItemRepository;
    private final WeightedPicker weightedPicker;
    private final ExteriorRoller exteriorRoller;
    private final StatTrackRoller StatTrackRoller;
    private final InventoryWearRoller inventoryWearRoller;
    private final CaseOpenRateLimiter caseOpenRateLimiter;
    private final AuthClient authClient;
    private final RecentDropRecorder recentDropRecorder;

    @Transactional
    public OpenCasesResponse open(Long userId, Long caseId) {
        return open(userId, caseId, 1);
    }

    @Transactional
    public OpenCasesResponse open(Long userId, Long caseId, Integer count) {
        int opens = normalizeCount(count);
        caseOpenRateLimiter.check(userId);

        GameCase gameCase = gameCaseRepository.findById(caseId)
                .filter(GameCase::isActive)
                .orElseThrow(() -> new CaseNotFoundException(caseId));

        int userLevel = authClient.getLevel(userId);
        int requiredLevel = gameCase.getMinLevel() == null ? 1 : gameCase.getMinLevel();
        if (userLevel < requiredLevel) {
            throw new CaseLevelTooLowException(caseId, requiredLevel, userLevel);
        }

        int countRequiredLevel = CaseOpenCountPolicy.requiredLevelForCount(opens);
        if (userLevel < countRequiredLevel) {
            throw new CaseOpenCountLevelTooLowException(opens, countRequiredLevel, userLevel);
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        BigDecimal unitPrice = gameCase.getPrice();
        BigDecimal totalCost = unitPrice.multiply(BigDecimal.valueOf(opens));
        if (wallet.getBalance().compareTo(totalCost) < 0) {
            throw new InsufficientFundsException();
        }

        List<CaseDrop> pool = caseDropRepository.findByCaseId(caseId);
        if (pool.isEmpty()) {
            throw new CaseDropPoolEmptyException(caseId);
        }

        Map<Long, Item> itemsById = itemRepository.findAllById(
                pool.stream().map(CaseDrop::getItemId).distinct().toList()
        ).stream().collect(Collectors.toMap(Item::getId, Function.identity()));

        List<OpenedCaseDropResponse> opened = new ArrayList<>(opens);
        for (int i = 0; i < opens; i++) {
            opened.add(rollOne(userId, gameCase, pool, itemsById));
        }

        wallet.setBalance(wallet.getBalance().subtract(totalCost));

        AuthExperienceResponse xp = authClient.addExperience(userId, CASE_OPEN_XP * opens);
        if (xp == null) {
            throw new IllegalStateException("Auth experience response was null for userId=" + userId);
        }

        BigDecimal levelUpReward = LevelUpCashReward.forLevelGain(
                userLevel,
                xp.level(),
                xp.leveledUp()
        );
        if (levelUpReward.signum() > 0) {
            wallet.setBalance(wallet.getBalance().add(levelUpReward));
        }
        walletRepository.save(wallet);

        return new OpenCasesResponse(
                opens,
                totalCost,
                wallet.getBalance(),
                List.copyOf(opened),
                xp.experienceGained(),
                xp.level(),
                xp.experience(),
                levelUpReward
        );
    }

    private OpenedCaseDropResponse rollOne(
            Long userId,
            GameCase gameCase,
            List<CaseDrop> pool,
            Map<Long, Item> itemsById
    ) {
        Long caseId = gameCase.getId();
        CaseDrop chosen = weightedPicker.pick(pool, CaseDrop::getWeight);
        Item item = itemsById.get(chosen.getItemId());
        if (item == null || !item.isActive()) {
            throw new CaseDropPoolEmptyException(caseId);
        }

        ItemExterior exterior = exteriorRoller.roll();
        boolean statTrack = StatTrackRoller.roll();
        InventoryWearRoller.Wear wear = inventoryWearRoller.roll(exterior);

        UserInventoryItem inventoryItem = new UserInventoryItem();
        inventoryItem.setUserId(userId);
        inventoryItem.setItemId(item.getId());
        inventoryItem.setExterior(exterior);
        inventoryItem.setStatTrack(statTrack);
        inventoryItem.setFloatValue(wear.floatValue());
        inventoryItem.setPaintSeed(wear.paintSeed());
        inventoryItem = inventoryItemRepository.save(inventoryItem);

        recentDropRecorder.record(
                userId,
                item,
                exterior,
                statTrack,
                RecentDropSource.CASE,
                caseId,
                gameCase.getCode()
        );

        return new OpenedCaseDropResponse(
                inventoryItem.getId(),
                item.getCode(),
                item.getDisplayName(),
                item.getRarity(),
                exterior,
                statTrack,
                wear.floatValue(),
                wear.paintSeed(),
                SellPricing.sellPrice(item.getRarity(), exterior, statTrack)
        );
    }

    private static int normalizeCount(Integer count) {
        int value = count == null ? MIN_OPEN_COUNT : count;
        if (value < MIN_OPEN_COUNT || value > MAX_OPEN_COUNT) {
            throw new InvalidCaseOpenCountException();
        }
        return value;
    }
}