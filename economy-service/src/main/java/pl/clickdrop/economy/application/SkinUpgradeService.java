package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.*;
import pl.clickdrop.economy.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.economy.application.exception.SkinUpgradeNoTargetsException;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkinUpgradeService {

    private static final double SUCCESS_CHANCE = 0.5;

    private final UserInventoryItemRepository userInventoryItemRepository;
    private final ItemRepository itemRepository;
    private final ExteriorRoller exteriorRoller;
    private final SkinUpgradeRng skinUpgradeRng;

    @Transactional(readOnly = true)
    public SkinUpgradePreviewResponse preview(Long userId, Long inventoryItemId) {
        UserInventoryItem row = requireOwned(userId, inventoryItemId);
        Item current = requireItem(row.getItemId(), inventoryItemId);

        BigDecimal currentPrice = SellPricing.sellPrice(current.getRarity(), row.getExterior());
        List<SkinUpgradeTargetDto> targets = findDearerTargets(currentPrice).stream()
                .map(item -> new SkinUpgradeTargetDto(
                        item.getId(),
                        item.getCode(),
                        item.getDisplayName(),
                        item.getRarity(),
                        SellPricing.sellPrice(item.getRarity(), ItemExterior.FIELD_TESTED)
                ))
                .toList();

        return new SkinUpgradePreviewResponse(
                row.getId(),
                new SkinUpgradeCurrentDto(
                        current.getCode(),
                        current.getDisplayName(),
                        current.getRarity(),
                        row.getExterior(),
                        currentPrice
                ),
                SUCCESS_CHANCE,
                targets
        );
    }

    @Transactional
    public SkinUpgradeResponse upgrade(Long userId, Long inventoryItemId) {
        UserInventoryItem row = requireOwned(userId, inventoryItemId);
        Item current = requireItem(row.getItemId(), inventoryItemId);

        BigDecimal currentPrice = SellPricing.sellPrice(current.getRarity(), row.getExterior());
        List<Item> targets = findDearerTargets(currentPrice);

        if (targets.isEmpty()) {
            throw new SkinUpgradeNoTargetsException(inventoryItemId);
        }

        if (!skinUpgradeRng.rollSuccess()) {
            userInventoryItemRepository.delete(row);
            return new SkinUpgradeFailResponse();
        }

        Item target = skinUpgradeRng.pickOne(targets);
        ItemExterior exterior = exteriorRoller.roll();

        userInventoryItemRepository.delete(row);

        UserInventoryItem upgraded = new UserInventoryItem();
        upgraded.setUserId(userId);
        upgraded.setItemId(target.getId());
        upgraded.setExterior(exterior);
        upgraded = userInventoryItemRepository.save(upgraded);

        return new SkinUpgradeSuccessResponse(
                upgraded.getId(),
                target.getCode(),
                target.getDisplayName(),
                target.getRarity(),
                exterior,
                SellPricing.sellPrice(target.getRarity(), exterior)
        );
    }

    private UserInventoryItem requireOwned(Long userId, Long inventoryItemId) {
        return userInventoryItemRepository.findByIdAndUserId(inventoryItemId, userId)
                .orElseThrow(() -> new InventoryItemNotFoundException(inventoryItemId));
    }
    private Item requireItem(Long itemId, Long inventoryItemId) {
        return itemRepository.findById(itemId)
                .filter(Item::isActive)
                .orElseThrow(() -> new InventoryItemNotFoundException(inventoryItemId));
    }
    private List<Item> findDearerTargets(BigDecimal currentPrice) {
        return itemRepository.findByActiveTrueOrderByIdAsc().stream()
                .filter(item -> SellPricing
                        .sellPrice(item.getRarity(), ItemExterior.FIELD_TESTED)
                        .compareTo(currentPrice) > 0)
                .toList();
    }
}
