package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.InventoryItemResponse;
import pl.clickdrop.economy.api.dto.SellInventoryItemResponse;
import pl.clickdrop.economy.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.economy.application.exception.WalletNotFoundException;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

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

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        BigDecimal soldFor = SellPricing.sellPrice(item.getRarity(), row.getExterior(), row.isStatTrack());

        wallet.setBalance(wallet.getBalance().add(soldFor));
        walletRepository.save(wallet);
        inventoryItemRepository.delete(row);

        return new SellInventoryItemResponse(wallet.getBalance(), soldFor);
    }
}
