package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyRewardServiceTest {

    @Mock
    DailyClaimRepository dailyClaimRepository;

    @Mock
    DailyDropRepository dailyDropRepository;

    @Mock
    ItemRepository itemRepository;

    @Mock
    UserInventoryItemRepository userInventoryItemRepository;

    @Mock
    WeightedPicker weightedPicker;

    @Mock
    ExteriorRoller exteriorRoller;

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
        DailyClaim claim = new DailyClaim();
        claim.setUserId(15L);
        claim.setClaimedAt(claimedAt);

        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.of(claim));

        DailyStatusResponse status = dailyRewardService.status(15L);

        assertThat(status.available()).isFalse();
        assertThat(status.nextClaimAt()).isEqualTo(claimedAt.plus(Duration.ofHours(24)));
    }

    @Test
    void status_available_whenCooldownElapsed() {
        Instant claimedAt = Instant.now().minus(Duration.ofHours(25));
        DailyClaim claim = new DailyClaim();
        claim.setUserId(15L);
        claim.setClaimedAt(claimedAt);

        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.of(claim));

        DailyStatusResponse status = dailyRewardService.status(15L);

        assertThat(status.available()).isTrue();
        assertThat(status.nextClaimAt()).isNull();
    }

    @Test
    void claim_firstTime_savesInventoryAndClaim() {
        DailyDrop drop = dailyDrop(1L, 3L, 50);
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

        Instant before = Instant.now();
        DailyClaimResponse res = dailyRewardService.claim(15L);
        Instant after = Instant.now();

        assertThat(res.inventoryItemId()).isEqualTo(99L);
        assertThat(res.itemCode()).isEqualTo("AK_REDLINE");
        assertThat(res.rarity()).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(res.exterior()).isEqualTo(ItemExterior.FIELD_TESTED);
        assertThat(res.suggestedSellPrice()).isEqualByComparingTo("40.00");
        assertThat(res.nextClaimAt()).isAfter(before.plus(Duration.ofHours(23)));
        assertThat(res.nextClaimAt()).isBefore(after.plus(Duration.ofHours(25)));

        ArgumentCaptor<UserInventoryItem> invCaptor = ArgumentCaptor.forClass(UserInventoryItem.class);
        verify(userInventoryItemRepository).save(invCaptor.capture());
        assertThat(invCaptor.getValue().getUserId()).isEqualTo(15L);
        assertThat(invCaptor.getValue().getItemId()).isEqualTo(3L);
        assertThat(invCaptor.getValue().getExterior()).isEqualTo(ItemExterior.FIELD_TESTED);

        ArgumentCaptor<DailyClaim> claimCaptor = ArgumentCaptor.forClass(DailyClaim.class);
        verify(dailyClaimRepository).save(claimCaptor.capture());
        assertThat(claimCaptor.getValue().getUserId()).isEqualTo(15L);
        assertThat(claimCaptor.getValue().getClaimedAt()).isNotNull();
    }

    @Test
    void claim_throws_whenWithin24h_andDoesNotSaveInventory() {
        Instant claimedAt = Instant.now().minus(Duration.ofHours(1));
        DailyClaim existing = new DailyClaim();
        existing.setUserId(15L);
        existing.setClaimedAt(claimedAt);

        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> dailyRewardService.claim(15L))
                .isInstanceOf(DailyAlreadyClaimedException.class)
                .satisfies(ex -> {
                    DailyAlreadyClaimedException dailyEx = (DailyAlreadyClaimedException) ex;
                    assertThat(dailyEx.getNextClaimAt())
                            .isEqualTo(claimedAt.plus(Duration.ofHours(24)));
                });

        verify(userInventoryItemRepository, never()).save(any());
        verify(dailyDropRepository, never()).findAll();
        verify(dailyClaimRepository, never()).save(any());
    }

    @Test
    void claim_afterCooldown_updatesExistingClaim() {
        Instant oldClaimAt = Instant.now().minus(Duration.ofHours(30));
        DailyClaim existing = new DailyClaim();
        existing.setId(7L);
        existing.setUserId(15L);
        existing.setClaimedAt(oldClaimAt);

        DailyDrop drop = dailyDrop(1L, 3L, 50);
        Item item = item(3L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);

        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.of(existing));
        when(dailyDropRepository.findAll()).thenReturn(List.of(drop));
        when(weightedPicker.pick(anyList(), any())).thenReturn(drop);
        when(itemRepository.findById(3L)).thenReturn(Optional.of(item));
        when(exteriorRoller.roll()).thenReturn(ItemExterior.MINIMAL_WEAR);
        when(userInventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(inv -> {
            UserInventoryItem row = inv.getArgument(0);
            row.setId(100L);
            return row;
        });
        when(dailyClaimRepository.save(any(DailyClaim.class))).thenAnswer(inv -> inv.getArgument(0));

        DailyClaimResponse res = dailyRewardService.claim(15L);

        assertThat(res.inventoryItemId()).isEqualTo(100L);
        assertThat(res.itemCode()).isEqualTo("USP_GUARDIAN");
        assertThat(res.suggestedSellPrice()).isEqualByComparingTo("18.00");

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

        verify(userInventoryItemRepository, never()).save(any());
        verify(dailyClaimRepository, never()).save(any());
    }

    @Test
    void claim_throws_whenChosenItemMissingOrInactive() {
        DailyDrop drop = dailyDrop(1L, 3L, 50);

        when(dailyClaimRepository.findByUserId(15L)).thenReturn(Optional.empty());
        when(dailyDropRepository.findAll()).thenReturn(List.of(drop));
        when(weightedPicker.pick(anyList(), any())).thenReturn(drop);
        when(itemRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dailyRewardService.claim(15L))
                .isInstanceOf(CaseDropPoolEmptyException.class);

        verify(userInventoryItemRepository, never()).save(any());
    }

    private static DailyDrop dailyDrop(Long id, Long itemId, int weight) {
        DailyDrop drop = new DailyDrop();
        drop.setId(id);
        drop.setItemId(itemId);
        drop.setWeight(weight);
        return drop;
    }

    private static Item item(Long id, String code, ItemRarity rarity) {
        Item item = new Item();
        item.setId(id);
        item.setCode(code);
        item.setRarity(rarity);
        item.setActive(true);
        return item;
    }
}