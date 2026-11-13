package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.SkinUpgradeFailResponse;
import pl.clickdrop.economy.api.dto.SkinUpgradePreviewResponse;
import pl.clickdrop.economy.api.dto.SkinUpgradeRequest;
import pl.clickdrop.economy.api.dto.SkinUpgradeResponse;
import pl.clickdrop.economy.api.dto.SkinUpgradeSuccessResponse;
import pl.clickdrop.economy.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.economy.application.exception.SkinUpgradeInvalidTargetException;
import pl.clickdrop.economy.application.exception.SkinUpgradeNoTargetsException;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkinUpgradeServiceTest {

    @Mock UserInventoryItemRepository inventoryItemRepository;
    @Mock ItemRepository itemRepository;
    @Mock SkinUpgradeRng skinUpgradeRng;
    @Mock InventoryWearRoller inventoryWearRoller;

    @InjectMocks
    SkinUpgradeService skinUpgradeService;

    @Test
    void preview_uspBattleScarred_includesBetterWearAndBandedDearerSkins() {
        UserInventoryItem row = SkinUpgradeTestFixtures.ownedRow(
                10L, 15L, 1L, ItemExterior.BATTLE_SCARRED, false);
        Item usp = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item restricted = SkinUpgradeTestFixtures.item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(usp));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(usp, restricted));

        SkinUpgradePreviewResponse preview = skinUpgradeService.preview(15L, 10L);

        assertThat(preview.successChance()).isEqualTo(0.5);
        assertThat(preview.current().itemCode()).isEqualTo("USP_GUARDIAN");
        assertThat(preview.current().statTrack()).isFalse();
        assertThat(preview.possibleTargets())
                .anyMatch(t -> t.itemCode().equals("USP_GUARDIAN")
                        && t.exterior() == ItemExterior.WELL_WORN
                        && !t.statTrack()
                        && t.suggestedSellPrice().compareTo(new BigDecimal("11.25")) == 0);
        assertThat(preview.possibleTargets())
                .noneMatch(t -> t.suggestedSellPrice().compareTo(new BigDecimal("13.13")) > 0);
        assertThat(preview.possibleTargets()).allMatch(t -> !t.statTrack());
    }

    @Test
    void preview_restrictedBattleScarred_capsBandNear35() {
        UserInventoryItem row = SkinUpgradeTestFixtures.ownedRow(
                10L, 15L, 2L, ItemExterior.BATTLE_SCARRED, false);
        Item usp = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item slate = SkinUpgradeTestFixtures.item(2L, "AK_47_SLATE", ItemRarity.RESTRICTED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(slate));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(usp, slate));

        SkinUpgradePreviewResponse preview = skinUpgradeService.preview(15L, 10L);

        assertThat(preview.possibleTargets())
                .anyMatch(t -> t.itemId().equals(2L) && t.exterior() == ItemExterior.WELL_WORN);
        assertThat(preview.possibleTargets())
                .noneMatch(t -> t.suggestedSellPrice().compareTo(new BigDecimal("35.00")) > 0);
    }

    @Test
    void upgrade_success_givesExactlyChosenTarget() {
        UserInventoryItem row = SkinUpgradeTestFixtures.ownedRow(
                10L, 15L, 1L, ItemExterior.FIELD_TESTED, false);
        Item current = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item dearer = SkinUpgradeTestFixtures.item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);
        SkinUpgradeRequest request = new SkinUpgradeRequest(2L, ItemExterior.BATTLE_SCARRED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(current));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(current, dearer));
        when(skinUpgradeRng.rollSuccess()).thenReturn(true);
        when(inventoryWearRoller.roll(ItemExterior.BATTLE_SCARRED))
                .thenReturn(new InventoryWearRoller.Wear(new BigDecimal("0.550000"), 88));
        when(inventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(inv -> {
            UserInventoryItem saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        SkinUpgradeResponse result = skinUpgradeService.upgrade(15L, 10L, request);

        assertThat(result).isInstanceOf(SkinUpgradeSuccessResponse.class);
        SkinUpgradeSuccessResponse success = (SkinUpgradeSuccessResponse) result;
        assertThat(success.itemCode()).isEqualTo("AK_REDLINE");
        assertThat(success.exterior()).isEqualTo(ItemExterior.BATTLE_SCARRED);
        assertThat(success.statTrack()).isFalse();
        assertThat(success.floatValue()).isEqualByComparingTo("0.550000");
        assertThat(success.paintSeed()).isEqualTo(88);
        assertThat(success.suggestedSellPrice()).isEqualByComparingTo("20.00");

        ArgumentCaptor<UserInventoryItem> captor = ArgumentCaptor.forClass(UserInventoryItem.class);
        verify(inventoryItemRepository).save(captor.capture());
        assertThat(captor.getValue().getItemId()).isEqualTo(2L);
        assertThat(captor.getValue().getExterior()).isEqualTo(ItemExterior.BATTLE_SCARRED);
        assertThat(captor.getValue().isStatTrack()).isFalse();
        assertThat(captor.getValue().getFloatValue()).isEqualByComparingTo("0.550000");
        assertThat(captor.getValue().getPaintSeed()).isEqualTo(88);
        verify(skinUpgradeRng, never()).pickOne(any());
    }

    @Test
    void upgrade_fail_deletesItem() {
        UserInventoryItem row = SkinUpgradeTestFixtures.ownedRow(
                10L, 15L, 1L, ItemExterior.FIELD_TESTED, false);
        Item current = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item dearer = SkinUpgradeTestFixtures.item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);
        SkinUpgradeRequest request = new SkinUpgradeRequest(2L, ItemExterior.BATTLE_SCARRED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(current));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(current, dearer));
        when(skinUpgradeRng.rollSuccess()).thenReturn(false);

        SkinUpgradeResponse result = skinUpgradeService.upgrade(15L, 10L, request);

        assertThat(result).isInstanceOf(SkinUpgradeFailResponse.class);
        verify(inventoryItemRepository).delete(row);
        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    void upgrade_throws_whenTargetOutOfBand() {
        UserInventoryItem row = SkinUpgradeTestFixtures.ownedRow(
                10L, 15L, 1L, ItemExterior.FIELD_TESTED, false);
        Item current = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item dearer = SkinUpgradeTestFixtures.item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);
        SkinUpgradeRequest request = new SkinUpgradeRequest(2L, ItemExterior.FIELD_TESTED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(current));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(current, dearer));

        assertThatThrownBy(() -> skinUpgradeService.upgrade(15L, 10L, request))
                .isInstanceOf(SkinUpgradeInvalidTargetException.class);

        verify(skinUpgradeRng, never()).rollSuccess();
        verify(inventoryItemRepository, never()).delete(any());
    }

    @Test
    void upgrade_throws_whenNotOwner() {
        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skinUpgradeService.upgrade(
                15L, 10L, new SkinUpgradeRequest(2L, ItemExterior.BATTLE_SCARRED)))
                .isInstanceOf(InventoryItemNotFoundException.class);
    }

    @Test
    void upgrade_throws_whenCovertFactoryNew_noTargets() {
        UserInventoryItem row = SkinUpgradeTestFixtures.ownedRow(
                10L, 15L, 1L, ItemExterior.FACTORY_NEW, false);
        Item covert = SkinUpgradeTestFixtures.item(1L, "AWP_ASIIMOV", ItemRarity.COVERT);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(covert));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(covert));

        assertThatThrownBy(() -> skinUpgradeService.upgrade(
                15L, 10L, new SkinUpgradeRequest(1L, ItemExterior.FACTORY_NEW)))
                .isInstanceOf(SkinUpgradeNoTargetsException.class);

        verify(skinUpgradeRng, never()).rollSuccess();
    }
}
