package pl.clickdrop.inventory.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.inventory.api.dto.InventoryItemResponse;
import pl.clickdrop.inventory.api.dto.SellInventoryBatchRequest;
import pl.clickdrop.inventory.api.dto.SellInventoryBatchResponse;
import pl.clickdrop.inventory.api.dto.SellInventoryItemResponse;
import pl.clickdrop.inventory.api.dto.SellSkipReason;
import pl.clickdrop.inventory.application.exception.InvalidSellBatchException;
import pl.clickdrop.inventory.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItem;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.inventory.domain.item.Item;
import pl.clickdrop.inventory.domain.item.ItemExterior;
import pl.clickdrop.inventory.domain.item.ItemRarity;
import pl.clickdrop.inventory.domain.item.ItemRepository;
import pl.clickdrop.inventory.domain.wallet.Wallet;
import pl.clickdrop.inventory.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock UserInventoryItemRepository inventoryItemRepository;
    @Mock ItemRepository itemRepository;
    @Mock WalletRepository walletRepository;

    @InjectMocks
    InventoryService inventoryService;

    @Test
    void listMine_mapsOwnedItems_withSuggestedSellPrice() {
        UserInventoryItem row = inventoryRow(99L, 15L, 3L, ItemExterior.FIELD_TESTED);
        Item item = item(3L, "AK_REDLINE", ItemRarity.RESTRICTED);

        when(inventoryItemRepository.findByUserIdOrderByCreatedAtDesc(15L))
                .thenReturn(List.of(row));
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(item));

        List<InventoryItemResponse> result = inventoryService.listMine(15L);

        assertThat(result).hasSize(1);
        InventoryItemResponse dto = result.getFirst();
        assertThat(dto.inventoryItemId()).isEqualTo(99L);
        assertThat(dto.code()).isEqualTo("AK_REDLINE");
        assertThat(dto.rarity()).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(dto.exterior()).isEqualTo(ItemExterior.FIELD_TESTED);
        assertThat(dto.statTrack()).isFalse();
        assertThat(dto.floatValue()).isEqualByComparingTo("0.250000");
        assertThat(dto.paintSeed()).isEqualTo(321);
        assertThat(dto.suggestedSellPrice()).isEqualByComparingTo("40.00");
    }

    @Test
    void sell_statTrack_appliesMultiplier() {
        UserInventoryItem row = inventoryRow(99L, 15L, 3L, ItemExterior.FIELD_TESTED);
        row.setStatTrack(true);
        Item item = item(3L, "AK_REDLINE", ItemRarity.RESTRICTED);
        Wallet wallet = wallet(15L, "10.00");

        when(inventoryItemRepository.findByIdAndUserId(99L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(3L)).thenReturn(Optional.of(item));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        SellInventoryItemResponse res = inventoryService.sell(15L, 99L);

        assertThat(res.soldFor()).isEqualByComparingTo("50.00");
        assertThat(res.balance()).isEqualByComparingTo("60.00");
    }

    @Test
    void listMine_returnsEmpty_whenNoRows() {
        when(inventoryItemRepository.findByUserIdOrderByCreatedAtDesc(15L))
                .thenReturn(List.of());

        assertThat(inventoryService.listMine(15L)).isEmpty();
        verifyNoInteractions(itemRepository);
    }

    @Test
    void sell_creditsWallet_andDeletesOwnItem() {
        UserInventoryItem row = inventoryRow(99L, 15L, 3L, ItemExterior.FACTORY_NEW);
        Item item = item(3L, "AWP_ASIIMOV", ItemRarity.COVERT);
        Wallet wallet = wallet(15L, "10.00");

        when(inventoryItemRepository.findByIdAndUserId(99L, 15L)).thenReturn(Optional.of(row));
        when(itemRepository.findById(3L)).thenReturn(Optional.of(item));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        SellInventoryItemResponse res = inventoryService.sell(15L, 99L);

        assertThat(res.soldFor()).isEqualByComparingTo("350.00");
        assertThat(res.balance()).isEqualByComparingTo("360.00");
        verify(inventoryItemRepository).deleteAll(List.of(row));
        verify(walletRepository).save(wallet);
    }

    @Test
    void sell_throws_whenItemNotOwned_andDoesNotCreditWallet() {
        when(inventoryItemRepository.findByIdAndUserId(99L, 15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.sell(15L, 99L))
                .isInstanceOf(InventoryItemNotFoundException.class);

        verify(walletRepository, never()).save(any());
        verify(inventoryItemRepository, never()).deleteAll(any());
    }

    @Test
    void sellBatch_sellsThreeOwnedItems() {
        UserInventoryItem a = inventoryRow(1L, 15L, 3L, ItemExterior.FIELD_TESTED);
        UserInventoryItem b = inventoryRow(2L, 15L, 3L, ItemExterior.FIELD_TESTED);
        UserInventoryItem c = inventoryRow(3L, 15L, 3L, ItemExterior.FIELD_TESTED);
        Item item = item(3L, "AK_REDLINE", ItemRarity.RESTRICTED);
        Wallet wallet = wallet(15L, "100.00");

        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(inventoryItemRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(a, b, c));
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(item));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        SellInventoryBatchResponse res = inventoryService.sellBatch(
                15L, new SellInventoryBatchRequest(List.of(1L, 2L, 3L)));

        assertThat(res.soldCount()).isEqualTo(3);
        assertThat(res.totalEarned()).isEqualByComparingTo("120.00");
        assertThat(res.balance()).isEqualByComparingTo("220.00");
        assertThat(res.sold()).hasSize(3);
        assertThat(res.skipped()).isEmpty();
        verify(inventoryItemRepository).deleteAll(List.of(a, b, c));
    }

    @Test
    void sellBatch_mixOwnedForeignMissing_bestEffort() {
        UserInventoryItem owned = inventoryRow(1L, 15L, 3L, ItemExterior.FIELD_TESTED);
        UserInventoryItem foreign = inventoryRow(2L, 99L, 3L, ItemExterior.FIELD_TESTED);
        Item item = item(3L, "AK_REDLINE", ItemRarity.RESTRICTED);
        Wallet wallet = wallet(15L, "10.00");

        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(inventoryItemRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(owned, foreign));
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(item));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        SellInventoryBatchResponse res = inventoryService.sellBatch(
                15L, new SellInventoryBatchRequest(List.of(1L, 2L, 3L)));

        assertThat(res.soldCount()).isEqualTo(1);
        assertThat(res.totalEarned()).isEqualByComparingTo("40.00");
        assertThat(res.balance()).isEqualByComparingTo("50.00");
        assertThat(res.skipped()).extracting(s -> s.reason())
                .containsExactly(SellSkipReason.NOT_OWNED, SellSkipReason.NOT_FOUND);
        verify(inventoryItemRepository).deleteAll(List.of(owned));
    }

    @Test
    void sellBatch_rejectsOverLimit() {
        List<Long> ids = new ArrayList<>(Collections.nCopies(51, 1L));

        assertThatThrownBy(() -> inventoryService.sellBatch(
                15L, new SellInventoryBatchRequest(ids)))
                .isInstanceOf(InvalidSellBatchException.class);

        verifyNoInteractions(walletRepository, inventoryItemRepository, itemRepository);
    }

    @Test
    void sellBatch_rejectsEmpty() {
        assertThatThrownBy(() -> inventoryService.sellBatch(
                15L, new SellInventoryBatchRequest(List.of())))
                .isInstanceOf(InvalidSellBatchException.class);

        verifyNoInteractions(walletRepository, inventoryItemRepository, itemRepository);
    }

    private static UserInventoryItem inventoryRow(
            Long id, Long userId, Long itemId, ItemExterior exterior
    ) {
        UserInventoryItem row = new UserInventoryItem();
        row.setId(id);
        row.setUserId(userId);
        row.setItemId(itemId);
        row.setExterior(exterior);
        row.setFloatValue(new BigDecimal("0.250000"));
        row.setPaintSeed(321);
        row.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return row;
    }

    private static Item item(Long id, String code, ItemRarity rarity) {
        Item i = new Item();
        i.setId(id);
        i.setCode(code);
        i.setDisplayName(code.replace('_', ' '));
        i.setRarity(rarity);
        i.setActive(true);
        return i;
    }

    private static Wallet wallet(Long userId, String balance) {
        Wallet w = new Wallet();
        w.setUserId(userId);
        w.setBalance(new BigDecimal(balance));
        return w;
    }
}