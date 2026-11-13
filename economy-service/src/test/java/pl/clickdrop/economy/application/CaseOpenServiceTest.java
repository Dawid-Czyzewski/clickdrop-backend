package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.OpenCasesResponse;
import pl.clickdrop.economy.application.client.AuthClient;
import pl.clickdrop.economy.application.client.AuthExperienceResponse;
import pl.clickdrop.economy.application.exception.CaseDropPoolEmptyException;
import pl.clickdrop.economy.application.exception.CaseNotFoundException;
import pl.clickdrop.economy.application.exception.CaseOpenRateLimitException;
import pl.clickdrop.economy.application.exception.InsufficientFundsException;
import pl.clickdrop.economy.application.exception.InvalidCaseOpenCountException;
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
import java.util.function.ToIntFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseOpenServiceTest {

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
    void open_x1_chargesWallet_savesInventory_grantsXp() {
        stubHappyPath("100.00");
        when(authClient.getLevel(15L)).thenReturn(1);
        when(authClient.addExperience(15L, 10))
                .thenReturn(new AuthExperienceResponse(1, 60, 10, false));

        OpenCasesResponse res = caseOpenService.open(15L, 1L, 1);

        assertThat(res.count()).isEqualTo(1);
        assertThat(res.totalCost()).isEqualByComparingTo("50.00");
        assertThat(res.balance()).isEqualByComparingTo("50.00");
        assertThat(res.drops()).hasSize(1);
        assertThat(res.drops().getFirst().floatValue()).isEqualByComparingTo("0.250000");
        assertThat(res.drops().getFirst().paintSeed()).isEqualTo(123);
        assertThat(res.experienceGained()).isEqualTo(10);

        ArgumentCaptor<UserInventoryItem> invCaptor = ArgumentCaptor.forClass(UserInventoryItem.class);
        verify(inventoryItemRepository, times(1)).save(invCaptor.capture());
        assertThat(invCaptor.getValue().getFloatValue()).isEqualByComparingTo("0.250000");
        assertThat(invCaptor.getValue().getPaintSeed()).isEqualTo(123);
        verify(authClient).getLevel(15L);
        verify(authClient).addExperience(15L, 10);
        verify(caseOpenRateLimiter).check(15L);
    }

    @Test
    void open_x3_opensThree_andChargesTriple() {
        stubHappyPath("200.00");
        when(authClient.getLevel(15L)).thenReturn(5);
        when(authClient.addExperience(15L, 30))
                .thenReturn(new AuthExperienceResponse(5, 20, 30, true));

        OpenCasesResponse res = caseOpenService.open(15L, 1L, 3);

        assertThat(res.count()).isEqualTo(3);
        assertThat(res.totalCost()).isEqualByComparingTo("150.00");
        assertThat(res.drops()).hasSize(3);
        verify(inventoryItemRepository, times(3)).save(any());
        verify(weightedPicker, times(3)).pick(anyList(), any(ToIntFunction.class));
        verify(authClient).addExperience(15L, 30);
    }

    @Test
    void open_rejectsInvalidCount() {
        assertThatThrownBy(() -> caseOpenService.open(15L, 1L, 0))
                .isInstanceOf(InvalidCaseOpenCountException.class);
        assertThatThrownBy(() -> caseOpenService.open(15L, 1L, 6))
                .isInstanceOf(InvalidCaseOpenCountException.class);

        verifyNoInteractions(gameCaseRepository, walletRepository, inventoryItemRepository, authClient);
    }

    @Test
    void open_x3_insufficientFunds_noSideEffects() {
        GameCase gameCase = CaseOpenTestFixtures.activeCase(1L, "50.00", 1);
        Wallet wallet = CaseOpenTestFixtures.wallet(15L, "100.00");

        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(gameCase));
        when(authClient.getLevel(15L)).thenReturn(5);
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> caseOpenService.open(15L, 1L, 3))
                .isInstanceOf(InsufficientFundsException.class);

        verify(inventoryItemRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
        verify(authClient, never()).addExperience(any(), anyInt());
        assertThat(wallet.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void open_doesNotCharge_whenRateLimited() {
        doThrow(new CaseOpenRateLimitException()).when(caseOpenRateLimiter).check(15L);

        assertThatThrownBy(() -> caseOpenService.open(15L, 1L, 1))
                .isInstanceOf(CaseOpenRateLimitException.class);

        verify(gameCaseRepository, never()).findById(any());
        verify(authClient, never()).addExperience(any(), anyInt());
    }

    @Test
    void open_throws_whenCaseMissing() {
        when(gameCaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOpenService.open(15L, 99L, 1))
                .isInstanceOf(CaseNotFoundException.class);

        verifyNoInteractions(walletRepository, caseDropRepository, inventoryItemRepository);
        verify(authClient, never()).getLevel(any());
        verify(authClient, never()).addExperience(any(), anyInt());
    }

    @Test
    void open_throws_whenDropPoolEmpty() {
        GameCase gameCase = CaseOpenTestFixtures.activeCase(1L, "50.00", 1);
        Wallet wallet = CaseOpenTestFixtures.wallet(15L, "100.00");

        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(gameCase));
        when(authClient.getLevel(15L)).thenReturn(1);
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(caseDropRepository.findByCaseId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> caseOpenService.open(15L, 1L, 1))
                .isInstanceOf(CaseDropPoolEmptyException.class);

        verify(inventoryItemRepository, never()).save(any());
        verify(authClient, never()).addExperience(any(), anyInt());
    }

    private void stubHappyPath(String balance) {
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
    }
}
