package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.DailyCashClaimResponse;
import pl.clickdrop.economy.api.dto.DailyCashDropResponse;
import pl.clickdrop.economy.api.dto.DailyClaimResponse;
import pl.clickdrop.economy.api.dto.DailyDropResponse;
import pl.clickdrop.economy.api.dto.DailySkinClaimResponse;
import pl.clickdrop.economy.api.dto.DailySkinDropResponse;
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
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static pl.clickdrop.economy.application.DailyRewardTestFixtures.*;

@ExtendWith(MockitoExtension.class)
class DailyRewardServiceTest {

    @Mock DailyClaimRepository dailyClaimRepository;
    @Mock DailyDropRepository dailyDropRepository;
    @Mock ItemRepository itemRepository;
    @Mock UserInventoryItemRepository userInventoryItemRepository;
    @Mock WalletRepository walletRepository;
    @Mock WeightedPicker weightedPicker;
    @Mock ExteriorRoller exteriorRoller;

    @InjectMocks
    DailyRewardService dailyRewardService;

    @Test
    void status_available_whenNeverClaimed() {
        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.empty());

        DailyStatusResponse status = dailyRewardService.status(15L);

        assertThat(status.available()).isTrue();
        assertThat(status.nextClaimAt()).isNull();
    }

    @Test
    void status_unavailable_whenClaimedRecently() {
        Instant claimedAt = Instant.now().minus(Duration.ofHours(2));
        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.of(claim(15L, claimedAt)));

        DailyStatusResponse status = dailyRewardService.status(15L);

        assertThat(status.available()).isFalse();
        assertThat(status.nextClaimAt()).isEqualTo(claimedAt.plus(Duration.ofHours(24)));
    }

    @Test
    void listDrops_returnsSkinAndCash() {
        when(dailyDropRepository.findAll()).thenReturn(List.of(skinDrop(1L, 3L, 40), cashDrop(2L, "25.00", 60)));
        when(itemRepository.findAllById(List.of(3L)))
                .thenReturn(List.of(item(3L, "AK_REDLINE", ItemRarity.RESTRICTED)));

        List<DailyDropResponse> drops = dailyRewardService.listDrops();

        assertThat(drops).hasSize(2);
        DailySkinDropResponse skin = (DailySkinDropResponse) drops.get(0);
        assertThat(skin.itemCode()).isEqualTo("AK_REDLINE");
        assertThat(((DailyCashDropResponse) drops.get(1)).cashAmount()).isEqualByComparingTo("25.00");
    }

    @Test
    void claim_skin_savesInventoryAndClaim() {
        DailyDrop drop = skinDrop(1L, 3L, 50);
        Item item = item(3L, "AK_REDLINE", ItemRarity.RESTRICTED);

        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.empty());
        when(dailyDropRepository.findAll()).thenReturn(List.of(drop));
        when(weightedPicker.pick(anyList(), any())).thenReturn(drop);
        when(itemRepository.findById(3L)).thenReturn(Optional.of(item));
        when(exteriorRoller.roll()).thenReturn(ItemExterior.FIELD_TESTED);
        when(userInventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(inv -> {
            UserInventoryItem row = inv.getArgument(0);
            row.setId(99L);
            return row;
        });
        when(dailyClaimRepository.save(any(DailyClaim.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyClaimResponse res = dailyRewardService.claim(15L);

        DailySkinClaimResponse skin = (DailySkinClaimResponse) res;
        assertThat(skin.inventoryItemId()).isEqualTo(99L);
        assertThat(skin.itemCode()).isEqualTo("AK_REDLINE");
        assertThat(skin.suggestedSellPrice()).isEqualByComparingTo("40.00");
        verify(walletRepository, never()).save(any());
        verify(dailyClaimRepository).save(any());
    }

    @Test
    void claim_cash_addsBalance_andDoesNotSaveInventory() {
        DailyDrop drop = cashDrop(1L, "25.00", 50);
        Wallet wallet = wallet(15L, "100.00");

        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.empty());
        when(dailyDropRepository.findAll()).thenReturn(List.of(drop));
        when(weightedPicker.pick(anyList(), any())).thenReturn(drop);
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dailyClaimRepository.save(any(DailyClaim.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyCashClaimResponse cash = (DailyCashClaimResponse) dailyRewardService.claim(15L);

        assertThat(cash.cashAmount()).isEqualByComparingTo("25.00");
        assertThat(cash.balance()).isEqualByComparingTo("125.00");
        verify(userInventoryItemRepository, never()).save(any());
        verify(walletRepository).save(wallet);
    }

    @Test
    void claim_throws_whenWithin24h_andDoesNotMutate() {
        Instant claimedAt = Instant.now().minus(Duration.ofHours(1));
        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.of(claim(15L, claimedAt)));

        assertThatThrownBy(() -> dailyRewardService.claim(15L))
                .isInstanceOf(DailyAlreadyClaimedException.class);

        verify(userInventoryItemRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
        verify(dailyDropRepository, never()).findAll();
    }

    @Test
    void claim_afterCooldown_updatesExistingClaim() {
        Instant oldClaimAt = Instant.now().minus(Duration.ofHours(30));
        DailyClaim existing = claim(15L, oldClaimAt);
        existing.setId(7L);
        DailyDrop drop = skinDrop(1L, 3L, 50);

        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.of(existing));
        when(dailyDropRepository.findAll()).thenReturn(List.of(drop));
        when(weightedPicker.pick(anyList(), any())).thenReturn(drop);
        when(itemRepository.findById(3L)).thenReturn(Optional.of(item(3L, "USP_GUARDIAN", ItemRarity.MIL_SPEC)));
        when(exteriorRoller.roll()).thenReturn(ItemExterior.MINIMAL_WEAR);
        when(userInventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(inv -> {
            UserInventoryItem row = inv.getArgument(0);
            row.setId(100L);
            return row;
        });
        when(dailyClaimRepository.save(any(DailyClaim.class))).thenAnswer(inv -> inv.getArgument(0));

        dailyRewardService.claim(15L);

        ArgumentCaptor<DailyClaim> claimCaptor = ArgumentCaptor.forClass(DailyClaim.class);
        verify(dailyClaimRepository).save(claimCaptor.capture());
        assertThat(claimCaptor.getValue().getId()).isEqualTo(7L);
        assertThat(claimCaptor.getValue().getClaimedAt()).isAfter(oldClaimAt);
    }

    @Test
    void claim_throws_whenDropPoolEmpty() {
        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.empty());
        when(dailyDropRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> dailyRewardService.claim(15L))
                .isInstanceOf(CaseDropPoolEmptyException.class);
    }

    @Test
    void claim_throws_whenChosenSkinMissing() {
        DailyDrop drop = skinDrop(1L, 3L, 50);
        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.empty());
        when(dailyDropRepository.findAll()).thenReturn(List.of(drop));
        when(weightedPicker.pick(anyList(), any())).thenReturn(drop);
        when(itemRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dailyRewardService.claim(15L))
                .isInstanceOf(CaseDropPoolEmptyException.class);
        verify(userInventoryItemRepository, never()).save(any());
    }
}
