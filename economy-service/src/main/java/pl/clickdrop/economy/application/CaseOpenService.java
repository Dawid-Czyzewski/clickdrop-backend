package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.OpenCaseResponse;
import pl.clickdrop.economy.application.client.AuthClient;
import pl.clickdrop.economy.application.client.AuthExperienceResponse;
import pl.clickdrop.economy.application.exception.CaseDropPoolEmptyException;
import pl.clickdrop.economy.application.exception.CaseNotFoundException;
import pl.clickdrop.economy.application.exception.InsufficientFundsException;
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
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseOpenService {

    private static final int CASE_OPEN_XP = 10;

    private final GameCaseRepository gameCaseRepository;
    private final CaseDropRepository caseDropRepository;
    private final ItemRepository itemRepository;
    private final WalletRepository walletRepository;
    private final UserInventoryItemRepository inventoryItemRepository;
    private final WeightedPicker weightedPicker;
    private final ExteriorRoller exteriorRoller;
    private final StatTrackRoller StatTrackRoller;
    private final CaseOpenRateLimiter caseOpenRateLimiter;
    private final AuthClient authClient;

    @Transactional
    public OpenCaseResponse open(Long userId, Long caseId) {
        caseOpenRateLimiter.check(userId);

        GameCase gameCase = gameCaseRepository.findById(caseId)
                .filter(GameCase::isActive)
                .orElseThrow(() -> new CaseNotFoundException(caseId));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        BigDecimal cost = gameCase.getPrice();
        if (wallet.getBalance().compareTo(cost) < 0) {
            throw new InsufficientFundsException();
        }

        List<CaseDrop> drops = caseDropRepository.findByCaseId(caseId);
        if (drops.isEmpty()) {
            throw new CaseDropPoolEmptyException(caseId);
        }

        CaseDrop chosenDrop = weightedPicker.pick(drops, CaseDrop::getWeight);
        Item item = itemRepository.findById(chosenDrop.getItemId())
                .filter(Item::isActive)
                .orElseThrow(() -> new CaseDropPoolEmptyException(caseId));

        ItemExterior exterior = exteriorRoller.roll();
        boolean statTrack = StatTrackRoller.roll();

        wallet.setBalance(wallet.getBalance().subtract(cost));
        walletRepository.save(wallet);

        UserInventoryItem inventoryItem = new UserInventoryItem();
        inventoryItem.setUserId(userId);
        inventoryItem.setItemId(item.getId());
        inventoryItem.setExterior(exterior);
        inventoryItem.setStatTrack(statTrack);
        inventoryItem = inventoryItemRepository.save(inventoryItem);

        BigDecimal suggested = SellPricing.sellPrice(item.getRarity(), exterior, statTrack);

        AuthExperienceResponse xp = authClient.addExperience(userId, CASE_OPEN_XP);
        if (xp == null) {
            throw new IllegalStateException("Auth experience response was null for userId=" + userId);
        }

        return new OpenCaseResponse(
                inventoryItem.getId(),
                item.getCode(),
                item.getDisplayName(),
                item.getRarity(),
                exterior,
                statTrack,
                wallet.getBalance(),
                cost,
                suggested,
                xp.experienceGained(),
                xp.level(),
                xp.experience()
        );
    }
}
