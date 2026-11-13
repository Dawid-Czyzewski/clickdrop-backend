package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.OpenCasesResponse;
import pl.clickdrop.economy.application.client.AuthClient;
import pl.clickdrop.economy.application.client.AuthExperienceResponse;
import pl.clickdrop.economy.domain.cases.CaseDrop;
import pl.clickdrop.economy.domain.cases.CaseDropRepository;
import pl.clickdrop.economy.domain.cases.GameCase;
import pl.clickdrop.economy.domain.cases.GameCaseRepository;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseOpenLevelUpRewardTest {

    @Mock GameCaseRepository gameCaseRepository;
    @Mock CaseDropRepository caseDropRepository;
    @Mock ItemRepository itemRepository;
    @Mock WalletRepository walletRepository;
    @Mock UserInventoryItemRepository inventoryItemRepository;
    @Mock WeightedPicker weightedPicker;
    @Mock ExteriorRoller exteriorRoller;
    @Mock StatTrackRoller StatTrackRoller;
    @Mock InventoryWearRoller inventoryWearRoller;
    @Mock CaseOpenRateLimiter caseOpenRateLimiter;
    @Mock AuthClient authClient;
    @Mock RecentDropRecorder recentDropRecorder;

    @InjectMocks
    CaseOpenService caseOpenService;

    @Test
    void open_noLevelUp_rewardIsZero_balanceOnlyCharged() {
        Wallet wallet = stubOpenable("100.00");
        when(authClient.getLevel(15L)).thenReturn(3);
        when(authClient.addExperience(15L, 10))
                .thenReturn(new AuthExperienceResponse(3, 40, 10, false));

        OpenCasesResponse res = caseOpenService.open(15L, 1L, 1);

        assertThat(res.levelUpReward()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(res.balance()).isEqualByComparingTo("50.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("50.00");
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void open_oneLevelUp_addsFiftyCd() {
        Wallet wallet = stubOpenable("100.00");
        when(authClient.getLevel(15L)).thenReturn(4);
        when(authClient.addExperience(15L, 10))
                .thenReturn(new AuthExperienceResponse(5, 10, 10, true));

        OpenCasesResponse res = caseOpenService.open(15L, 1L, 1);

        assertThat(res.levelUpReward()).isEqualByComparingTo("50.00");
        assertThat(res.balance()).isEqualByComparingTo("100.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("100.00");
        assertThat(res.level()).isEqualTo(5);
    }

    @Test
    void open_multiLevelUp_addsFiftyTimesLevelsGained() {
        Wallet wallet = stubOpenable("200.00");
        when(authClient.getLevel(15L)).thenReturn(5);
        when(authClient.addExperience(15L, 30))
                .thenReturn(new AuthExperienceResponse(8, 20, 30, true));

        OpenCasesResponse res = caseOpenService.open(15L, 1L, 3);

        assertThat(res.levelUpReward()).isEqualByComparingTo("150.00");
        assertThat(res.balance()).isEqualByComparingTo("200.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("200.00");
        assertThat(res.level()).isEqualTo(8);
    }

    @Test
    void forLevelGain_matchesZeroOneAndN() {
        assertThat(LevelUpCashReward.forLevelGain(3, 3, false)).isEqualByComparingTo("0");
        assertThat(LevelUpCashReward.forLevelGain(4, 5, true)).isEqualByComparingTo("50.00");
        assertThat(LevelUpCashReward.forLevelGain(2, 5, true)).isEqualByComparingTo("150.00");
        assertThat(LevelUpCashReward.forLevelGain(5, 4, true)).isEqualByComparingTo("0");
    }

    private Wallet stubOpenable(String balance) {
        GameCase gameCase = CaseOpenTestFixtures.activeCase(1L, "50.00", 1);
        Wallet wallet = CaseOpenTestFixtures.wallet(15L, balance);
        CaseDrop drop = CaseOpenTestFixtures.drop(10L, 1L, 3L, 15);
        Item item = CaseOpenTestFixtures.item(3L, "AK_REDLINE", ItemRarity.RESTRICTED);
        AtomicLong ids = new AtomicLong(100);

        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(gameCase));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(caseDropRepository.findByCaseId(1L)).thenReturn(List.of(drop));
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(item));
        when(weightedPicker.pick(anyList(), any())).thenReturn(drop);
        when(exteriorRoller.roll()).thenReturn(ItemExterior.FIELD_TESTED);
        when(StatTrackRoller.roll()).thenReturn(false);
        when(inventoryWearRoller.roll(ItemExterior.FIELD_TESTED))
                .thenReturn(new InventoryWearRoller.Wear(new BigDecimal("0.250000"), 123));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(i -> {
            UserInventoryItem row = i.getArgument(0);
            row.setId(ids.getAndIncrement());
            return row;
        });
        return wallet;
    }
}
