package pl.clickdrop.inventory.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.inventory.api.dto.SkinUpgradePreviewResponse;
import pl.clickdrop.inventory.api.dto.SkinUpgradeRequest;
import pl.clickdrop.inventory.api.dto.SkinUpgradeResponse;
import pl.clickdrop.inventory.api.dto.SkinUpgradeSuccessResponse;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItem;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.inventory.domain.item.Item;
import pl.clickdrop.inventory.domain.item.ItemExterior;
import pl.clickdrop.inventory.domain.item.ItemRarity;
import pl.clickdrop.inventory.domain.item.ItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkinUpgradeStatTrackInheritTest {

    @Mock UserInventoryItemRepository inventoryItemRepository;
    @Mock ItemRepository itemRepository;
    @Mock SkinUpgradeRng skinUpgradeRng;
    @Mock InventoryWearRoller inventoryWearRoller;

    @InjectMocks
    SkinUpgradeService skinUpgradeService;

    @Test
    void preview_statTrackInput_exposesStatTrackTargetsWithStPrices() {
        UserInventoryItem row = SkinUpgradeTestFixtures.ownedRow(
                10L, 15L, 1L, ItemExterior.BATTLE_SCARRED, true);
        Item usp = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item restricted = SkinUpgradeTestFixtures.item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(usp));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(usp, restricted));

        SkinUpgradePreviewResponse preview = skinUpgradeService.preview(15L, 10L);

        assertThat(preview.current().statTrack()).isTrue();
        assertThat(preview.current().suggestedSellPrice())
                .isEqualByComparingTo(SellPricing.sellPrice(
                        ItemRarity.MIL_SPEC, ItemExterior.BATTLE_SCARRED, true));
        assertThat(preview.possibleTargets()).isNotEmpty();
        assertThat(preview.possibleTargets()).allMatch(t -> t.statTrack());
        assertThat(preview.possibleTargets())
                .anyMatch(t -> t.itemCode().equals("USP_GUARDIAN")
                        && t.exterior() == ItemExterior.WELL_WORN
                        && t.suggestedSellPrice().compareTo(
                                SellPricing.sellPrice(
                                        ItemRarity.MIL_SPEC, ItemExterior.WELL_WORN, true)) == 0);
    }

    @Test
    void upgrade_success_inheritsStatTrackFromSource() {
        UserInventoryItem row = SkinUpgradeTestFixtures.ownedRow(
                10L, 15L, 1L, ItemExterior.FIELD_TESTED, true);
        Item current = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item dearer = SkinUpgradeTestFixtures.item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);
        SkinUpgradeRequest request = new SkinUpgradeRequest(2L, ItemExterior.BATTLE_SCARRED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(current));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(current, dearer));
        when(skinUpgradeRng.rollSuccess()).thenReturn(true);
        when(inventoryWearRoller.roll(ItemExterior.BATTLE_SCARRED))
                .thenReturn(new InventoryWearRoller.Wear(new BigDecimal("0.550000"), 9));
        when(inventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(inv -> {
            UserInventoryItem saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        SkinUpgradeResponse result = skinUpgradeService.upgrade(15L, 10L, request);

        assertThat(result).isInstanceOf(SkinUpgradeSuccessResponse.class);
        SkinUpgradeSuccessResponse success = (SkinUpgradeSuccessResponse) result;
        assertThat(success.statTrack()).isTrue();
        assertThat(success.floatValue()).isEqualByComparingTo("0.550000");
        assertThat(success.paintSeed()).isEqualTo(9);
        assertThat(success.suggestedSellPrice()).isEqualByComparingTo(
                SellPricing.sellPrice(ItemRarity.RESTRICTED, ItemExterior.BATTLE_SCARRED, true));

        ArgumentCaptor<UserInventoryItem> captor = ArgumentCaptor.forClass(UserInventoryItem.class);
        verify(inventoryItemRepository).save(captor.capture());
        assertThat(captor.getValue().isStatTrack()).isTrue();
    }
}