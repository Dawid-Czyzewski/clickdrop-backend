package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.SkinUpgradeFailResponse;
import pl.clickdrop.economy.api.dto.SkinUpgradePreviewResponse;
import pl.clickdrop.economy.api.dto.SkinUpgradeResponse;
import pl.clickdrop.economy.api.dto.SkinUpgradeSuccessResponse;
import pl.clickdrop.economy.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.economy.application.exception.SkinUpgradeNoTargetsException;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkinUpgradeServiceTest {

    @Mock UserInventoryItemRepository inventoryItemRepository;
    @Mock ItemRepository itemRepository;
    @Mock ExteriorRoller exteriorRoller;
    @Mock SkinUpgradeRng skinUpgradeRng;

    @InjectMocks
    SkinUpgradeService skinUpgradeService;

    @Test
    void preview_returnsCurrent_chanceAndTargets() {
        UserInventoryItem row = ownedRow(10L, 15L, 1L, ItemExterior.FIELD_TESTED);
        Item current = item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item dearer = item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(current));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(current, dearer));

        SkinUpgradePreviewResponse preview = skinUpgradeService.preview(15L, 10L);

        assertThat(preview.inventoryItemId()).isEqualTo(10L);
        assertThat(preview.successChance()).isEqualTo(0.5);
        assertThat(preview.current().itemCode()).isEqualTo("USP_GUARDIAN");
        assertThat(preview.possibleTargets()).hasSize(1);
        assertThat(preview.possibleTargets().getFirst().itemCode()).isEqualTo("AK_REDLINE");
    }

    @Test
    void upgrade_success_replacesWithDearerSkin() {
        UserInventoryItem row = ownedRow(10L, 15L, 1L, ItemExterior.FIELD_TESTED);
        Item current = item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item dearer = item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(current));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(current, dearer));
        when(skinUpgradeRng.rollSuccess()).thenReturn(true);
        when(skinUpgradeRng.pickOne(anyList())).thenReturn(dearer);
        when(exteriorRoller.roll()).thenReturn(ItemExterior.MINIMAL_WEAR);
        when(inventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(inv -> {
            UserInventoryItem saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        SkinUpgradeResponse result = skinUpgradeService.upgrade(15L, 10L);

        assertThat(result).isInstanceOf(SkinUpgradeSuccessResponse.class);
        SkinUpgradeSuccessResponse success = (SkinUpgradeSuccessResponse) result;
        assertThat(success.inventoryItemId()).isEqualTo(99L);
        assertThat(success.itemCode()).isEqualTo("AK_REDLINE");
        assertThat(success.suggestedSellPrice()).isEqualByComparingTo("48.00");

        verify(inventoryItemRepository).delete(row);
        ArgumentCaptor<UserInventoryItem> captor = ArgumentCaptor.forClass(UserInventoryItem.class);
        verify(inventoryItemRepository).save(captor.capture());
        assertThat(captor.getValue().getItemId()).isEqualTo(2L);
        assertThat(captor.getValue().getUserId()).isEqualTo(15L);
    }

    @Test
    void upgrade_fail_deletesItem() {
        UserInventoryItem row = ownedRow(10L, 15L, 1L, ItemExterior.FIELD_TESTED);
        Item current = item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item dearer = item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(current));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(current, dearer));
        when(skinUpgradeRng.rollSuccess()).thenReturn(false);

        SkinUpgradeResponse result = skinUpgradeService.upgrade(15L, 10L);

        assertThat(result).isInstanceOf(SkinUpgradeFailResponse.class);
        verify(inventoryItemRepository).delete(row);
        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    void upgrade_throws_whenNotOwner() {
        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skinUpgradeService.upgrade(15L, 10L))
                .isInstanceOf(InventoryItemNotFoundException.class);

        verify(inventoryItemRepository, never()).delete(any());
    }

    @Test
    void upgrade_throws_whenNoTargets() {
        UserInventoryItem row = ownedRow(10L, 15L, 1L, ItemExterior.FACTORY_NEW);
        Item covert = item(1L, "AWP_ASIIMOV", ItemRarity.COVERT);

        when(inventoryItemRepository.findByIdAndUserId(10L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(covert));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(covert));

        assertThatThrownBy(() -> skinUpgradeService.upgrade(15L, 10L))
                .isInstanceOf(SkinUpgradeNoTargetsException.class);

        verify(inventoryItemRepository, never()).delete(any());
        verify(skinUpgradeRng, never()).rollSuccess();
    }

    private static UserInventoryItem ownedRow(Long id, Long userId, Long itemId, ItemExterior exterior) {
        UserInventoryItem row = new UserInventoryItem();
        row.setId(id);
        row.setUserId(userId);
        row.setItemId(itemId);
        row.setExterior(exterior);
        return row;
    }

    private static Item item(Long id, String code, ItemRarity rarity) {
        Item item = new Item();
        item.setId(id);
        item.setCode(code);
        item.setDisplayName(code.replace('_', ' '));
        item.setRarity(rarity);
        item.setActive(true);
        return item;
    }
}