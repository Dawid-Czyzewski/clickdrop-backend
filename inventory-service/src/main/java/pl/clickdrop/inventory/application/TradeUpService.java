package pl.clickdrop.inventory.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeUpService {

    private final UserInventoryItemRepository inventoryItemRepository;
    private final ItemRepository itemRepository;
    private final TradeUpRng tradeUpRng;
    private final InventoryWearRoller inventoryWearRoller;

    @Transactional
    public TradeUpResponse tradeUp(Long userId, TradeUpRequest request) {
        List<Long> ids = TradeUpRules.requireExactUniqueIds(
                request == null ? null : request.inventoryItemIds());

        List<UserInventoryItem> rows = loadOwnedRows(userId, ids);
        List<Item> sourceItems = loadActiveCatalog(rows);

        ItemRarity sourceRarity = requireSameRarity(sourceItems);
        ItemRarity targetRarity = TradeUpRules.nextRarity(sourceRarity);
        if (targetRarity == null) {
            throw new TradeUpNoTargetsException(sourceRarity);
        }

        List<Item> pool = itemRepository.findByActiveTrueOrderByIdAsc().stream()
                .filter(item -> item.getRarity() == targetRarity)
                .toList();
        if (pool.isEmpty()) {
            throw new TradeUpNoTargetsException(sourceRarity);
        }

        ItemExterior exterior = TradeUpRules.averageExterior(
                rows.stream().map(UserInventoryItem::getExterior).toList());
        boolean statTrack = TradeUpRules.resultStatTrack(
                rows.stream().map(UserInventoryItem::isStatTrack).toList());

        Item chosen = tradeUpRng.pickOne(pool);

        inventoryItemRepository.deleteAll(rows);

        InventoryWearRoller.Wear wear = inventoryWearRoller.roll(exterior);

        UserInventoryItem created = new UserInventoryItem();
        created.setUserId(userId);
        created.setItemId(chosen.getId());
        created.setExterior(exterior);
        created.setStatTrack(statTrack);
        created.setFloatValue(wear.floatValue());
        created.setPaintSeed(wear.paintSeed());
        created = inventoryItemRepository.save(created);

        return new TradeUpResponse(
                created.getId(),
                chosen.getId(),
                chosen.getCode(),
                chosen.getDisplayName(),
                chosen.getRarity(),
                exterior,
                statTrack,
                wear.floatValue(),
                wear.paintSeed(),
                SellPricing.sellPrice(chosen.getRarity(), exterior, statTrack)
        );
    }

    private List<UserInventoryItem> loadOwnedRows(Long userId, List<Long> ids) {
        Map<Long, UserInventoryItem> byId = inventoryItemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UserInventoryItem::getId, Function.identity()));

        List<UserInventoryItem> rows = new ArrayList<>(ids.size());
        for (Long id : ids) {
            UserInventoryItem row = byId.get(id);
            if (row == null || !Objects.equals(row.getUserId(), userId)) {
                throw new InventoryItemNotFoundException(id);
            }
            rows.add(row);
        }
        return rows;
    }

    private List<Item> loadActiveCatalog(List<UserInventoryItem> rows) {
        Map<Long, Item> byId = itemRepository.findAllById(
                rows.stream().map(UserInventoryItem::getItemId).toList()
        ).stream().collect(Collectors.toMap(Item::getId, Function.identity()));

        List<Item> items = new ArrayList<>(rows.size());
        for (UserInventoryItem row : rows) {
            Item item = byId.get(row.getItemId());
            if (item == null || !item.isActive()) {
                throw new TradeUpInvalidException(
                        "Trade-up source item inactive or missing: inventoryItemId=" + row.getId());
            }
            items.add(item);
        }
        return items;
    }

    private static ItemRarity requireSameRarity(List<Item> items) {
        ItemRarity rarity = items.getFirst().getRarity();
        boolean mixed = items.stream().anyMatch(item -> item.getRarity() != rarity);
        if (mixed) {
            throw new TradeUpInvalidException("Trade-up requires the same rarity for all items");
        }
        return rarity;
    }
}
