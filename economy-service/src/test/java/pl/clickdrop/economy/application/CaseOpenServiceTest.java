package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.OpenCaseResponse;
import pl.clickdrop.economy.application.exception.CaseDropPoolEmptyException;
import pl.clickdrop.economy.application.exception.CaseNotFoundException;
import pl.clickdrop.economy.application.exception.InsufficientFundsException;
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

    @InjectMocks
    CaseOpenService caseOpenService;

    @Test
    void open_chargesWallet_savesInventory_andReturnsSuggestedSellPrice() {
        GameCase gameCase = activeCase(1L, "50.00");
        Wallet wallet = wallet(15L, "100.00");
        CaseDrop drop = drop(10L, 1L, 3L, 15);
        Item item = item(3L, "AK_REDLINE", ItemRarity.RESTRICTED);

        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(gameCase));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(caseDropRepository.findByCaseId(1L)).thenReturn(List.of(drop));
        when(weightedPicker.pick(anyList(), any())).thenReturn(drop);
        when(itemRepository.findById(3L)).thenReturn(Optional.of(item));
        when(exteriorRoller.roll()).thenReturn(ItemExterior.FIELD_TESTED);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(i -> {
            UserInventoryItem row = i.getArgument(0);
            row.setId(99L);
            return row;
        });

        OpenCaseResponse res = caseOpenService.open(15L, 1L);

        assertThat(res.inventoryItemId()).isEqualTo(99L);
        assertThat(res.itemCode()).isEqualTo("AK_REDLINE");
        assertThat(res.rarity()).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(res.exterior()).isEqualTo(ItemExterior.FIELD_TESTED);
        assertThat(res.cost()).isEqualByComparingTo("50.00");
        assertThat(res.balance()).isEqualByComparingTo("50.00");
        assertThat(res.suggestedSellPrice()).isEqualByComparingTo("40.00");

        ArgumentCaptor<UserInventoryItem> captor = ArgumentCaptor.forClass(UserInventoryItem.class);
        verify(inventoryItemRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(15L);
        assertThat(captor.getValue().getItemId()).isEqualTo(3L);
        assertThat(captor.getValue().getExterior()).isEqualTo(ItemExterior.FIELD_TESTED);

        verify(weightedPicker).pick(eq(List.of(drop)), any(ToIntFunction.class));
    }

    @Test
    void open_throws_whenInsufficientFunds_andDoesNotSaveInventory() {
        GameCase gameCase = activeCase(1L, "50.00");
        Wallet wallet = wallet(15L, "10.00");

        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(gameCase));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> caseOpenService.open(15L, 1L))
                .isInstanceOf(InsufficientFundsException.class);

        verify(inventoryItemRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
        assertThat(wallet.getBalance()).isEqualByComparingTo("10.00");
    }

    @Test
    void open_throws_whenCaseMissing() {
        when(gameCaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOpenService.open(15L, 99L))
                .isInstanceOf(CaseNotFoundException.class);

        verifyNoInteractions(walletRepository, caseDropRepository, inventoryItemRepository);
    }

    @Test
    void open_throws_whenCaseInactive() {
        GameCase inactive = activeCase(1L, "50.00");
        inactive.setActive(false);
        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> caseOpenService.open(15L, 1L))
                .isInstanceOf(CaseNotFoundException.class);
    }

    @Test
    void open_throws_whenDropPoolEmpty() {
        GameCase gameCase = activeCase(1L, "50.00");
        Wallet wallet = wallet(15L, "100.00");

        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(gameCase));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(caseDropRepository.findByCaseId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> caseOpenService.open(15L, 1L))
                .isInstanceOf(CaseDropPoolEmptyException.class);

        verify(inventoryItemRepository, never()).save(any());
    }

    private static GameCase activeCase(Long id, String price) {
        GameCase c = new GameCase();
        c.setId(id);
        c.setCode("ANVIL");
        c.setPrice(new BigDecimal(price));
        c.setActive(true);
        return c;
    }

    private static Wallet wallet(Long userId, String balance) {
        Wallet w = new Wallet();
        w.setUserId(userId);
        w.setBalance(new BigDecimal(balance));
        return w;
    }

    private static CaseDrop drop(Long id, Long caseId, Long itemId, int weight) {
        CaseDrop d = new CaseDrop();
        d.setId(id);
        d.setCaseId(caseId);
        d.setItemId(itemId);
        d.setWeight(weight);
        return d;
    }

    private static Item item(Long id, String code, ItemRarity rarity) {
        Item i = new Item();
        i.setId(id);
        i.setCode(code);
        i.setRarity(rarity);
        i.setActive(true);
        return i;
    }
}