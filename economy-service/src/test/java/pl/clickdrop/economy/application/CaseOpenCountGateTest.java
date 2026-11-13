package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.OpenCasesResponse;
import pl.clickdrop.economy.application.client.AuthClient;
import pl.clickdrop.economy.application.client.AuthExperienceResponse;
import pl.clickdrop.economy.application.exception.CaseOpenCountLevelTooLowException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseOpenCountGateTest {

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
    void open_x1_allowedAtLevel1() {
        stubOpenable();
        when(authClient.getLevel(15L)).thenReturn(1);
        when(authClient.addExperience(15L, 10))
                .thenReturn(new AuthExperienceResponse(1, 10, 10, false));

        OpenCasesResponse res = caseOpenService.open(15L, 1L, 1);

        assertThat(res.count()).isEqualTo(1);
        verify(walletRepository).save(any());
        verify(authClient).addExperience(15L, 10);
    }

    @Test
    void open_x3_throws_whenBelowLevel5_noSideEffects() {
        GameCase gameCase = CaseOpenTestFixtures.activeCase(1L, "50.00", 1);
        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(gameCase));
        when(authClient.getLevel(15L)).thenReturn(4);

        assertThatThrownBy(() -> caseOpenService.open(15L, 1L, 3))
                .isInstanceOf(CaseOpenCountLevelTooLowException.class)
                .satisfies(ex -> {
                    CaseOpenCountLevelTooLowException e = (CaseOpenCountLevelTooLowException) ex;
                    assertThat(e.getRequestedCount()).isEqualTo(3);
                    assertThat(e.getRequiredLevel()).isEqualTo(5);
                    assertThat(e.getUserLevel()).isEqualTo(4);
                });

        verifyNoChargeOrXp();
    }

    @Test
    void open_x5_throws_whenBelowLevel10_noSideEffects() {
        GameCase gameCase = CaseOpenTestFixtures.activeCase(1L, "50.00", 1);
        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(gameCase));
        when(authClient.getLevel(15L)).thenReturn(9);

        assertThatThrownBy(() -> caseOpenService.open(15L, 1L, 5))
                .isInstanceOf(CaseOpenCountLevelTooLowException.class)
                .satisfies(ex -> {
                    CaseOpenCountLevelTooLowException e = (CaseOpenCountLevelTooLowException) ex;
                    assertThat(e.getRequestedCount()).isEqualTo(5);
                    assertThat(e.getRequiredLevel()).isEqualTo(10);
                    assertThat(e.getUserLevel()).isEqualTo(9);
                });

        verifyNoChargeOrXp();
    }

    @Test
    void open_x3_allowedAtLevel5() {
        stubOpenable();
        when(authClient.getLevel(15L)).thenReturn(5);
        when(authClient.addExperience(15L, 30))
                .thenReturn(new AuthExperienceResponse(5, 30, 30, false));

        OpenCasesResponse res = caseOpenService.open(15L, 1L, 3);

        assertThat(res.count()).isEqualTo(3);
        verify(inventoryItemRepository, times(3)).save(any());
        verify(authClient).addExperience(15L, 30);
    }

    @Test
    void open_x5_allowedAtLevel10() {
        stubOpenable();
        when(authClient.getLevel(15L)).thenReturn(10);
        when(authClient.addExperience(15L, 50))
                .thenReturn(new AuthExperienceResponse(10, 50, 50, false));

        OpenCasesResponse res = caseOpenService.open(15L, 1L, 5);

        assertThat(res.count()).isEqualTo(5);
        verify(inventoryItemRepository, times(5)).save(any());
        verify(authClient).addExperience(15L, 50);
    }

    @Test
    void maxOpenCountForLevel_matchesGates() {
        assertThat(CaseOpenCountPolicy.maxOpenCountForLevel(1)).isEqualTo(1);
        assertThat(CaseOpenCountPolicy.maxOpenCountForLevel(4)).isEqualTo(1);
        assertThat(CaseOpenCountPolicy.maxOpenCountForLevel(5)).isEqualTo(3);
        assertThat(CaseOpenCountPolicy.maxOpenCountForLevel(9)).isEqualTo(3);
        assertThat(CaseOpenCountPolicy.maxOpenCountForLevel(10)).isEqualTo(5);
    }

    private void verifyNoChargeOrXp() {
        verify(walletRepository, never()).findByUserId(any());
        verify(walletRepository, never()).save(any());
        verify(inventoryItemRepository, never()).save(any());
        verify(authClient, never()).addExperience(any(), anyInt());
        verify(recentDropRecorder, never()).record(any(), any(), any(), anyBoolean(), any(), any(), any());
    }

    private void stubOpenable() {
        GameCase gameCase = CaseOpenTestFixtures.activeCase(1L, "50.00", 1);
        Wallet wallet = CaseOpenTestFixtures.wallet(15L, "500.00");
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
    }
}
