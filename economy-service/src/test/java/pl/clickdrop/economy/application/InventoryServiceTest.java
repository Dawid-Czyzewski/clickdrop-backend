package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.InventoryItemResponse;
import pl.clickdrop.economy.api.dto.SellInventoryItemResponse;
import pl.clickdrop.economy.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.time.Instant;
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
        assertThat(dto.suggestedSellPrice()).isEqualByComparingTo("40.00");
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
        verify(inventoryItemRepository).delete(row);
        verify(walletRepository).save(wallet);
    }

    @Test
    void sell_throws_whenItemNotOwned_andDoesNotCreditWallet() {
        when(inventoryItemRepository.findByIdAndUserId(99L, 15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.sell(15L, 99L))
                .isInstanceOf(InventoryItemNotFoundException.class);

        verify(walletRepository, never()).save(any());
        verify(inventoryItemRepository, never()).delete(any());
    }

    private static UserInventoryItem inventoryRow(
            Long id, Long userId, Long itemId, ItemExterior exterior
    ) {
        UserInventoryItem row = new UserInventoryItem();
        row.setId(id);
        row.setUserId(userId);
        row.setItemId(itemId);
        row.setExterior(exterior);
        row.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return row;
    }

    private static Item item(Long id, String code, ItemRarity rarity) {
        Item i = new Item();
        i.setId(id);
        i.setCode(code);
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