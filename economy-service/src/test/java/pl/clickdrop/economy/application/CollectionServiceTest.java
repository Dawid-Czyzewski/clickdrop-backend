package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.CollectionClaimResponse;
import pl.clickdrop.economy.api.dto.CollectionDetailResponse;
import pl.clickdrop.economy.api.dto.CollectionProgressResponse;
import pl.clickdrop.economy.application.exception.CollectionAlreadyClaimedException;
import pl.clickdrop.economy.application.exception.CollectionIncompleteException;
import pl.clickdrop.economy.application.exception.CollectionNotFoundException;
import pl.clickdrop.economy.domain.collection.Collection;
import pl.clickdrop.economy.domain.collection.CollectionItem;
import pl.clickdrop.economy.domain.collection.CollectionItemRepository;
import pl.clickdrop.economy.domain.collection.CollectionRepository;
import pl.clickdrop.economy.domain.collection.UserCollectionClaim;
import pl.clickdrop.economy.domain.collection.UserCollectionClaimRepository;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    private static final Long USER = 15L;

    @Mock CollectionRepository collectionRepository;
    @Mock CollectionItemRepository collectionItemRepository;
    @Mock UserCollectionClaimRepository claimRepository;
    @Mock UserInventoryItemRepository inventoryItemRepository;
    @Mock ItemRepository itemRepository;
    @Mock WalletRepository walletRepository;

    @InjectMocks
    CollectionService collectionService;

    @Test
    void list_emptyInventory_ownedZeroAndNotClaimed() {
        Collection col = collection(1L, "CS20_CASE", "CS20 Case", "150.00", 1);
        when(collectionRepository.findByActiveTrueOrderBySortOrderAscIdAsc()).thenReturn(List.of(col));
        when(collectionItemRepository.findByCollectionIdIn(List.of(1L)))
                .thenReturn(List.of(ci(1L, 10L), ci(1L, 11L)));
        when(claimRepository.findByUserIdAndCollectionIdIn(USER, List.of(1L))).thenReturn(List.of());
        when(inventoryItemRepository.findDistinctItemIdsOwned(eq(USER), anyCollection()))
                .thenReturn(List.of());

        List<CollectionProgressResponse> result = collectionService.list(USER);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().owned()).isZero();
        assertThat(result.getFirst().total()).isEqualTo(2);
        assertThat(result.getFirst().completed()).isFalse();
        assertThat(result.getFirst().rewardClaimed()).isFalse();
        assertThat(result.getFirst().rewardCd()).isEqualByComparingTo("150.00");
    }

    @Test
    void list_allItemsOwned_completedTrue() {
        Collection col = collection(1L, "CS20_CASE", "CS20 Case", "150.00", 1);
        when(collectionRepository.findByActiveTrueOrderBySortOrderAscIdAsc()).thenReturn(List.of(col));
        when(collectionItemRepository.findByCollectionIdIn(List.of(1L)))
                .thenReturn(List.of(ci(1L, 10L), ci(1L, 11L)));
        when(claimRepository.findByUserIdAndCollectionIdIn(USER, List.of(1L))).thenReturn(List.of());
        when(inventoryItemRepository.findDistinctItemIdsOwned(eq(USER), anyCollection()))
                .thenReturn(List.of(10L, 11L));

        CollectionProgressResponse row = collectionService.list(USER).getFirst();

        assertThat(row.owned()).isEqualTo(2);
        assertThat(row.completed()).isTrue();
        assertThat(row.rewardClaimed()).isFalse();
    }

    @Test
    void get_detail_marksOwnedPerItem() {
        Collection col = collection(1L, "CS20_CASE", "CS20 Case", "150.00", 1);
        Item a = item(10L, "USP_A", ItemRarity.MIL_SPEC);
        Item b = item(11L, "AK_B", ItemRarity.RESTRICTED);

        when(collectionRepository.findByCodeAndActiveTrue("CS20_CASE")).thenReturn(Optional.of(col));
        when(collectionItemRepository.findByCollectionIdOrderByItemIdAsc(1L))
                .thenReturn(List.of(ci(1L, 10L), ci(1L, 11L)));
        when(inventoryItemRepository.findDistinctItemIdsOwned(eq(USER), anyCollection()))
                .thenReturn(List.of(10L));
        when(claimRepository.existsByUserIdAndCollectionId(USER, 1L)).thenReturn(false);
        when(itemRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(a, b));

        CollectionDetailResponse detail = collectionService.get(USER, "CS20_CASE");

        assertThat(detail.owned()).isEqualTo(1);
        assertThat(detail.total()).isEqualTo(2);
        assertThat(detail.items()).hasSize(2);
        assertThat(detail.items().getFirst().owned()).isTrue();
        assertThat(detail.items().get(1).owned()).isFalse();
    }

    @Test
    void claim_ok_creditsWalletAndSavesClaim() {
        Collection col = collection(1L, "CS20_CASE", "CS20 Case", "150.00", 1);
        Wallet wallet = wallet(USER, "100.00");

        when(collectionRepository.findByCodeAndActiveTrue("CS20_CASE")).thenReturn(Optional.of(col));
        when(claimRepository.existsByUserIdAndCollectionId(USER, 1L)).thenReturn(false);
        when(collectionItemRepository.findByCollectionIdOrderByItemIdAsc(1L))
                .thenReturn(List.of(ci(1L, 10L), ci(1L, 11L)));
        when(inventoryItemRepository.findDistinctItemIdsOwned(eq(USER), anyCollection()))
                .thenReturn(List.of(10L, 11L));
        when(walletRepository.findByUserId(USER)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.save(any(UserCollectionClaim.class))).thenAnswer(inv -> inv.getArgument(0));

        CollectionClaimResponse result = collectionService.claim(USER, "CS20_CASE");

        assertThat(result.rewardCd()).isEqualByComparingTo("150.00");
        assertThat(result.balance()).isEqualByComparingTo("250.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("250.00");

        ArgumentCaptor<UserCollectionClaim> claimCaptor = ArgumentCaptor.forClass(UserCollectionClaim.class);
        verify(claimRepository).save(claimCaptor.capture());
        assertThat(claimCaptor.getValue().getUserId()).isEqualTo(USER);
        assertThat(claimCaptor.getValue().getCollectionId()).isEqualTo(1L);
    }

    @Test
    void claim_incomplete_throwsWithoutSideEffects() {
        Collection col = collection(1L, "CS20_CASE", "CS20 Case", "150.00", 1);
        when(collectionRepository.findByCodeAndActiveTrue("CS20_CASE")).thenReturn(Optional.of(col));
        when(claimRepository.existsByUserIdAndCollectionId(USER, 1L)).thenReturn(false);
        when(collectionItemRepository.findByCollectionIdOrderByItemIdAsc(1L))
                .thenReturn(List.of(ci(1L, 10L), ci(1L, 11L)));
        when(inventoryItemRepository.findDistinctItemIdsOwned(eq(USER), anyCollection()))
                .thenReturn(List.of(10L));

        assertThatThrownBy(() -> collectionService.claim(USER, "CS20_CASE"))
                .isInstanceOf(CollectionIncompleteException.class);

        verify(walletRepository, never()).save(any());
        verify(claimRepository, never()).save(any());
    }

    @Test
    void claim_twice_secondThrowsAlreadyClaimed() {
        Collection col = collection(1L, "CS20_CASE", "CS20 Case", "150.00", 1);
        when(collectionRepository.findByCodeAndActiveTrue("CS20_CASE")).thenReturn(Optional.of(col));
        when(claimRepository.existsByUserIdAndCollectionId(USER, 1L)).thenReturn(true);

        assertThatThrownBy(() -> collectionService.claim(USER, "CS20_CASE"))
                .isInstanceOf(CollectionAlreadyClaimedException.class);

        verify(walletRepository, never()).findByUserId(any());
        verify(claimRepository, never()).save(any());
    }

    @Test
    void get_unknownCode_throws404() {
        when(collectionRepository.findByCodeAndActiveTrue("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.get(USER, "NOPE"))
                .isInstanceOf(CollectionNotFoundException.class);
    }

    private static Collection collection(
            Long id, String code, String name, String reward, int sortOrder
    ) {
        Collection col = new Collection();
        col.setId(id);
        col.setCode(code);
        col.setDisplayName(name);
        col.setRewardCd(new BigDecimal(reward));
        col.setSortOrder(sortOrder);
        col.setActive(true);
        return col;
    }

    private static CollectionItem ci(Long collectionId, Long itemId) {
        CollectionItem row = new CollectionItem();
        row.setCollectionId(collectionId);
        row.setItemId(itemId);
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

    private static Wallet wallet(Long userId, String balance) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal(balance));
        return wallet;
    }
}
