package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.CollectionClaimResponse;
import pl.clickdrop.economy.api.dto.CollectionDetailResponse;
import pl.clickdrop.economy.api.dto.CollectionItemProgressDto;
import pl.clickdrop.economy.api.dto.CollectionProgressResponse;
import pl.clickdrop.economy.application.exception.CollectionAlreadyClaimedException;
import pl.clickdrop.economy.application.exception.CollectionIncompleteException;
import pl.clickdrop.economy.application.exception.CollectionNotFoundException;
import pl.clickdrop.economy.application.exception.WalletNotFoundException;
import pl.clickdrop.economy.domain.collection.Collection;
import pl.clickdrop.economy.domain.collection.CollectionItem;
import pl.clickdrop.economy.domain.collection.CollectionItemRepository;
import pl.clickdrop.economy.domain.collection.CollectionRepository;
import pl.clickdrop.economy.domain.collection.UserCollectionClaim;
import pl.clickdrop.economy.domain.collection.UserCollectionClaimRepository;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final UserCollectionClaimRepository claimRepository;
    private final UserInventoryItemRepository inventoryItemRepository;
    private final ItemRepository itemRepository;
    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public List<CollectionProgressResponse> list(Long userId) {
        List<Collection> collections = collectionRepository.findByActiveTrueOrderBySortOrderAscIdAsc();
        if (collections.isEmpty()) {
            return List.of();
        }

        List<Long> collectionIds = collections.stream().map(Collection::getId).toList();
        Map<Long, List<Long>> itemIdsByCollection = groupItemIds(collectionIds);
        Set<Long> claimedIds = claimedCollectionIds(userId, collectionIds);
        Set<Long> allItemIds = itemIdsByCollection.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        Set<Long> ownedItemIds = ownedItemIds(userId, allItemIds);

        return collections.stream()
                .map(col -> toProgress(
                        col,
                        itemIdsByCollection.getOrDefault(col.getId(), List.of()),
                        ownedItemIds,
                        claimedIds.contains(col.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CollectionDetailResponse get(Long userId, String code) {
        Collection collection = requireActive(code);
        List<Long> itemIds = collectionItemRepository
                .findByCollectionIdOrderByItemIdAsc(collection.getId())
                .stream()
                .map(CollectionItem::getItemId)
                .toList();

        Set<Long> ownedItemIds = ownedItemIds(userId, itemIds);
        boolean claimed = claimRepository.existsByUserIdAndCollectionId(userId, collection.getId());

        Map<Long, Item> itemsById = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        List<CollectionItemProgressDto> items = itemIds.stream()
                .map(itemId -> {
                    Item item = itemsById.get(itemId);
                    if (item == null) {
                        return null;
                    }
                    return new CollectionItemProgressDto(
                            item.getId(),
                            item.getCode(),
                            item.getDisplayName(),
                            item.getRarity(),
                            ownedItemIds.contains(itemId)
                    );
                })
                .filter(dto -> dto != null)
                .toList();

        int owned = (int) items.stream().filter(CollectionItemProgressDto::owned).count();
        int total = items.size();

        return new CollectionDetailResponse(
                collection.getCode(),
                collection.getDisplayName(),
                collection.getSortOrder(),
                owned,
                total,
                owned == total && total > 0,
                collection.getRewardCd(),
                claimed,
                items
        );
    }

    @Transactional
    public CollectionClaimResponse claim(Long userId, String code) {
        Collection collection = requireActive(code);

        if (claimRepository.existsByUserIdAndCollectionId(userId, collection.getId())) {
            throw new CollectionAlreadyClaimedException(code);
        }

        List<Long> itemIds = collectionItemRepository
                .findByCollectionIdOrderByItemIdAsc(collection.getId())
                .stream()
                .map(CollectionItem::getItemId)
                .toList();

        Set<Long> ownedItemIds = ownedItemIds(userId, itemIds);
        if (itemIds.isEmpty() || ownedItemIds.size() < itemIds.size()) {
            throw new CollectionIncompleteException(code);
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
        wallet.setBalance(wallet.getBalance().add(collection.getRewardCd()));
        walletRepository.save(wallet);

        UserCollectionClaim claim = new UserCollectionClaim();
        claim.setUserId(userId);
        claim.setCollectionId(collection.getId());
        claim.setClaimedAt(Instant.now());
        claimRepository.save(claim);

        return new CollectionClaimResponse(
                collection.getCode(),
                collection.getRewardCd(),
                wallet.getBalance()
        );
    }

    private Collection requireActive(String code) {
        return collectionRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new CollectionNotFoundException(code));
    }

    private Map<Long, List<Long>> groupItemIds(List<Long> collectionIds) {
        return collectionItemRepository.findByCollectionIdIn(collectionIds).stream()
                .collect(Collectors.groupingBy(
                        CollectionItem::getCollectionId,
                        Collectors.mapping(CollectionItem::getItemId, Collectors.toList())
                ));
    }

    private Set<Long> claimedCollectionIds(Long userId, List<Long> collectionIds) {
        return claimRepository.findByUserIdAndCollectionIdIn(userId, collectionIds).stream()
                .map(UserCollectionClaim::getCollectionId)
                .collect(Collectors.toSet());
    }

    private Set<Long> ownedItemIds(Long userId, java.util.Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(inventoryItemRepository.findDistinctItemIdsOwned(userId, itemIds));
    }

    private static CollectionProgressResponse toProgress(
            Collection collection,
            List<Long> itemIds,
            Set<Long> ownedItemIds,
            boolean claimed
    ) {
        int total = itemIds.size();
        int owned = (int) itemIds.stream().filter(ownedItemIds::contains).count();
        return new CollectionProgressResponse(
                collection.getCode(),
                collection.getDisplayName(),
                collection.getSortOrder(),
                owned,
                total,
                owned == total && total > 0,
                collection.getRewardCd(),
                claimed
        );
    }
}
