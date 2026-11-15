package pl.clickdrop.inventory.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.inventory.api.dto.*;
import pl.clickdrop.inventory.application.exception.InvalidSellBatchException;
import pl.clickdrop.inventory.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.inventory.application.exception.WalletNotFoundException;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItem;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.inventory.domain.item.Item;
import pl.clickdrop.inventory.domain.item.ItemRepository;
import pl.clickdrop.inventory.domain.wallet.Wallet;
import pl.clickdrop.inventory.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    static final int MAX_SELL_BATCH_SIZE = 50;

    private final UserInventoryItemRepository inventoryItemRepository;
    private final ItemRepository itemRepository;
    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> listMine(Long userId) {
        List<UserInventoryItem> owned =
                inventoryItemRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (owned.isEmpty()) {
            return List.of();
        }

        Map<Long, Item> itemById = itemRepository.findAllById(
               owned.stream().map(UserInventoryItem::getItemId).toList()
        ).stream().collect(Collectors.toMap(Item::getId, Function.identity()));

        return owned.stream()
                .map(row -> {
                    Item item = itemById.get(row.getItemId());
                    if (item == null) {
                        return null;
                    }

                    return new InventoryItemResponse(
                            row.getId(),
                            item.getId(),
                            item.getCode(),
                            item.getDisplayName(),
                            item.getRarity(),
                            row.getExterior(),
                            row.isStatTrack(),
                            row.getFloatValue(),
                            row.getPaintSeed(),
                            SellPricing.sellPrice(item.getRarity(), row.getExterior(), row.isStatTrack()),
                            row.getCreatedAt()
                    );
                }).filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public SellInventoryItemResponse sell(Long userId, Long inventoryItemId) {
        UserInventoryItem row = inventoryItemRepository
                .findByIdAndUserId(inventoryItemId, userId)
                .orElseThrow(() -> new InventoryItemNotFoundException(inventoryItemId));

        Item item = itemRepository.findById(row.getItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(inventoryItemId));

        Wallet wallet = requireWallet(userId);
        BigDecimal soldFor = priceOf(row, item);
        creditAndDelete(wallet, soldFor, List.of(row));

        return new SellInventoryItemResponse(wallet.getBalance(), soldFor);
    }

    @Transactional
    public SellInventoryBatchResponse sellBatch(Long userId, SellInventoryBatchRequest request) {
        List<Long> ids = validatedUniqueIds(request);

        Wallet wallet = requireWallet(userId);

        Map<Long, UserInventoryItem> byId = inventoryItemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UserInventoryItem::getId, Function.identity()));

        Set<Long> catalogIds = byId.values().stream()
                .filter(row -> Objects.equals(row.getUserId(), userId))
                .map(UserInventoryItem::getItemId)
                .collect(Collectors.toSet());

        Map<Long, Item> itemsById = itemRepository.findAllById(catalogIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        List<SoldInventoryItemDto> sold = new ArrayList<>();
        List<SkippedInventoryItemDto> skipped = new ArrayList<>();
        List<UserInventoryItem> toDelete = new ArrayList<>();
        BigDecimal totalEarned = BigDecimal.ZERO;

        for (Long id : ids) {
            UserInventoryItem row = byId.get(id);

            if (row == null) {
                skipped.add(new SkippedInventoryItemDto(id, SellSkipReason.NOT_FOUND));
                continue;
            }

            if (!Objects.equals(row.getUserId(), userId)) {
                skipped.add(new SkippedInventoryItemDto(id, SellSkipReason.NOT_OWNED));
                continue;
            }

            Item item = itemsById.get(row.getItemId());
            if (item == null) {
                skipped.add(new SkippedInventoryItemDto(id, SellSkipReason.NOT_FOUND));
                continue;
            }

            BigDecimal earned = priceOf(row, item);
            totalEarned = totalEarned.add(earned);
            toDelete.add(row);
            sold.add(new SoldInventoryItemDto(id, item.getCode(), earned));
        }

        if (!toDelete.isEmpty()) {
            creditAndDelete(wallet, totalEarned, toDelete);
        }

        return new SellInventoryBatchResponse(
                sold.size(),
                totalEarned,
                wallet.getBalance(),
                List.copyOf(sold),
                List.copyOf(skipped)
        );
    }

    private static List<Long> validatedUniqueIds(SellInventoryBatchRequest request) {
        List<Long> raw = request == null ? null : request.inventoryItemIds();

        if (raw == null || raw.isEmpty()) {
            throw new InvalidSellBatchException();
        }

        if (raw.size() > MAX_SELL_BATCH_SIZE) {
            throw new InvalidSellBatchException();
        }

        if (raw.stream().anyMatch(Objects::isNull)) {
            throw new InvalidSellBatchException();
        }

        return List.copyOf(new LinkedHashSet<>(raw));
    }

    private Wallet requireWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    private static BigDecimal priceOf(UserInventoryItem row, Item item) {
        return SellPricing.sellPrice(item.getRarity(), row.getExterior(), row.isStatTrack());
    }

    private void creditAndDelete(Wallet wallet, BigDecimal amount, List<UserInventoryItem> rows) {
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        inventoryItemRepository.deleteAll(rows);
    }
}
