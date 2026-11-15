package pl.clickdrop.inventory.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.inventory.api.dto.TradeUpRequest;
import pl.clickdrop.inventory.api.dto.TradeUpResponse;
import pl.clickdrop.inventory.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.inventory.application.exception.TradeUpInvalidException;
import pl.clickdrop.inventory.application.exception.TradeUpNoTargetsException;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItem;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.inventory.domain.item.Item;
import pl.clickdrop.inventory.domain.item.ItemExterior;
import pl.clickdrop.inventory.domain.item.ItemRarity;
import pl.clickdrop.inventory.domain.item.ItemRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeUpServiceTest {

    private static final Long USER = 15L;

    @Mock UserInventoryItemRepository inventoryItemRepository;
    @Mock ItemRepository itemRepository;
    @Mock TradeUpRng tradeUpRng;
    @Mock InventoryWearRoller inventoryWearRoller;

    @InjectMocks
    TradeUpService tradeUpService;

    @Test
    void tradeUp_ok_deletesTenAndCreatesHigherRarity() {
        List<Long> ids = tenIds();
        Item milSpec = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item restricted = SkinUpgradeTestFixtures.item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);
        List<UserInventoryItem> rows = ownedRows(ids, 1L, ItemExterior.FIELD_TESTED, false);

        when(inventoryItemRepository.findAllById(ids)).thenReturn(rows);
        when(itemRepository.findAllById(anyList())).thenReturn(List.of(milSpec));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(milSpec, restricted));
        when(tradeUpRng.pickOne(List.of(restricted))).thenReturn(restricted);
        when(inventoryWearRoller.roll(ItemExterior.FIELD_TESTED))
                .thenReturn(new InventoryWearRoller.Wear(new BigDecimal("0.250000"), 77));
        when(inventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(inv -> {
            UserInventoryItem saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        TradeUpResponse result = tradeUpService.tradeUp(USER, new TradeUpRequest(ids));

        assertThat(result.inventoryItemId()).isEqualTo(99L);
        assertThat(result.code()).isEqualTo("AK_REDLINE");
        assertThat(result.rarity()).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(result.exterior()).isEqualTo(ItemExterior.FIELD_TESTED);
        assertThat(result.statTrack()).isFalse();
        assertThat(result.floatValue()).isEqualByComparingTo("0.250000");
        assertThat(result.paintSeed()).isEqualTo(77);
        assertThat(result.suggestedSellPrice()).isEqualByComparingTo("40.00");

        verify(inventoryItemRepository).deleteAll(rows);
        ArgumentCaptor<UserInventoryItem> captor = ArgumentCaptor.forClass(UserInventoryItem.class);
        verify(inventoryItemRepository).save(captor.capture());
        assertThat(captor.getValue().getItemId()).isEqualTo(2L);
        assertThat(captor.getValue().isStatTrack()).isFalse();
        assertThat(captor.getValue().getFloatValue()).isEqualByComparingTo("0.250000");
        assertThat(captor.getValue().getPaintSeed()).isEqualTo(77);
    }

    @Test
    void tradeUp_allStatTrack_resultIsStatTrack() {
        List<Long> ids = tenIds();
        Item milSpec = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item restricted = SkinUpgradeTestFixtures.item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);
        List<UserInventoryItem> rows = ownedRows(ids, 1L, ItemExterior.MINIMAL_WEAR, true);

        when(inventoryItemRepository.findAllById(ids)).thenReturn(rows);
        when(itemRepository.findAllById(anyList())).thenReturn(List.of(milSpec));
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(milSpec, restricted));
        when(tradeUpRng.pickOne(List.of(restricted))).thenReturn(restricted);
        when(inventoryWearRoller.roll(ItemExterior.MINIMAL_WEAR))
                .thenReturn(new InventoryWearRoller.Wear(new BigDecimal("0.100000"), 11));
        when(inventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(inv -> {
            UserInventoryItem saved = inv.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        TradeUpResponse result = tradeUpService.tradeUp(USER, new TradeUpRequest(ids));

        assertThat(result.statTrack()).isTrue();
        assertThat(result.exterior()).isEqualTo(ItemExterior.MINIMAL_WEAR);
        assertThat(result.floatValue()).isEqualByComparingTo("0.100000");
        assertThat(result.paintSeed()).isEqualTo(11);
        assertThat(result.suggestedSellPrice()).isEqualByComparingTo("60.00");
    }

    @Test
    void tradeUp_notExactlyTen_throws400() {
        assertThatThrownBy(() -> tradeUpService.tradeUp(USER, new TradeUpRequest(List.of(1L, 2L))))
                .isInstanceOf(TradeUpInvalidException.class);
    }

    @Test
    void tradeUp_mixedRarity_throws400() {
        List<Long> ids = tenIds();
        Item milSpec = SkinUpgradeTestFixtures.item(1L, "USP_GUARDIAN", ItemRarity.MIL_SPEC);
        Item restricted = SkinUpgradeTestFixtures.item(2L, "AK_REDLINE", ItemRarity.RESTRICTED);

        List<UserInventoryItem> rows = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            rows.add(SkinUpgradeTestFixtures.ownedRow(
                    ids.get(i), USER, 1L, ItemExterior.FIELD_TESTED, false));
        }
        rows.add(SkinUpgradeTestFixtures.ownedRow(
                ids.get(9), USER, 2L, ItemExterior.FIELD_TESTED, false));

        when(inventoryItemRepository.findAllById(ids)).thenReturn(rows);
        when(itemRepository.findAllById(anyList())).thenReturn(List.of(milSpec, restricted));

        assertThatThrownBy(() -> tradeUpService.tradeUp(USER, new TradeUpRequest(ids)))
                .isInstanceOf(TradeUpInvalidException.class)
                .hasMessageContaining("same rarity");
    }

    @Test
    void tradeUp_foreignItem_throws404() {
        List<Long> ids = tenIds();
        List<UserInventoryItem> rows = ownedRows(ids, 1L, ItemExterior.FIELD_TESTED, false);
        rows.get(3).setUserId(999L);

        when(inventoryItemRepository.findAllById(ids)).thenReturn(rows);

        assertThatThrownBy(() -> tradeUpService.tradeUp(USER, new TradeUpRequest(ids)))
                .isInstanceOf(InventoryItemNotFoundException.class);
    }

    @Test
    void tradeUp_covert_hasNoTargets() {
        List<Long> ids = tenIds();
        Item covert = SkinUpgradeTestFixtures.item(9L, "AWP_DRAGON", ItemRarity.COVERT);
        List<UserInventoryItem> rows = ownedRows(ids, 9L, ItemExterior.FIELD_TESTED, false);

        when(inventoryItemRepository.findAllById(ids)).thenReturn(rows);
        when(itemRepository.findAllById(anyList())).thenReturn(List.of(covert));

        assertThatThrownBy(() -> tradeUpService.tradeUp(USER, new TradeUpRequest(ids)))
                .isInstanceOf(TradeUpNoTargetsException.class);
    }

    private static List<Long> tenIds() {
        return LongStream.rangeClosed(1, 10).boxed().toList();
    }

    private static List<UserInventoryItem> ownedRows(
            List<Long> ids, Long itemId, ItemExterior exterior, boolean statTrack
    ) {
        List<UserInventoryItem> rows = new ArrayList<>(ids.size());
        for (Long id : ids) {
            rows.add(SkinUpgradeTestFixtures.ownedRow(id, USER, itemId, exterior, statTrack));
        }
        return rows;
    }
}