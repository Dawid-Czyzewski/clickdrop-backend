package pl.clickdrop.inventory.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.inventory.api.dto.*;
import pl.clickdrop.inventory.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.inventory.application.exception.SkinUpgradeInvalidTargetException;
import pl.clickdrop.inventory.application.exception.SkinUpgradeNoTargetsException;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItem;
import pl.clickdrop.inventory.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.inventory.domain.item.Item;
import pl.clickdrop.inventory.domain.item.ItemExterior;
import pl.clickdrop.inventory.domain.item.ItemRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkinUpgradeService {

    private static final double SUCCESS_CHANCE = 0.5;
    private static final BigDecimal MAX_PRICE_MULTIPLIER = new BigDecimal("1.75");

    private final UserInventoryItemRepository userInventoryItemRepository;
    private final ItemRepository itemRepository;
    private final SkinUpgradeRng skinUpgradeRng;
    private final InventoryWearRoller inventoryWearRoller;

    @Transactional(readOnly = true)
    public SkinUpgradePreviewResponse preview(Long userId, Long inventoryItemId) {
        UserInventoryItem row = requireOwned(userId, inventoryItemId);
        Item current = requireItem(row.getItemId(), inventoryItemId);

        boolean sourceStatTrack = row.isStatTrack();
        BigDecimal currentPrice = SellPricing.sellPrice(
                current.getRarity(), row.getExterior(), sourceStatTrack);
        List<SkinUpgradeTargetDto> targets = findDearerTargets(currentPrice, sourceStatTrack).stream()
                .map(SkinUpgradeService::toDto)
                .toList();

        return new SkinUpgradePreviewResponse(
                row.getId(),
                new SkinUpgradeCurrentDto(
                        current.getCode(),
                        current.getDisplayName(),
                        current.getRarity(),
                        row.getExterior(),
                        sourceStatTrack,
                        currentPrice
                ),
                SUCCESS_CHANCE,
                targets
        );
    }

    @Transactional
    public SkinUpgradeResponse upgrade(Long userId, Long inventoryItemId, SkinUpgradeRequest request) {
        UserInventoryItem row = requireOwned(userId, inventoryItemId);
        Item current = requireItem(row.getItemId(), inventoryItemId);

        boolean sourceStatTrack = row.isStatTrack();
        BigDecimal currentPrice = SellPricing.sellPrice(
                current.getRarity(), row.getExterior(), sourceStatTrack);
        List<UpgradeTarget> targets = findDearerTargets(currentPrice, sourceStatTrack);

        if (targets.isEmpty()) {
            throw new SkinUpgradeNoTargetsException(inventoryItemId);
        }

        UpgradeTarget chosen = resolveChosenTarget(targets, request, inventoryItemId);

        if (!skinUpgradeRng.rollSuccess()) {
            userInventoryItemRepository.delete(row);
            return new SkinUpgradeFailResponse();
        }

        userInventoryItemRepository.delete(row);

        boolean statTrack = sourceStatTrack;
        BigDecimal price = SellPricing.sellPrice(
                chosen.item().getRarity(), chosen.exterior(), statTrack);

        InventoryWearRoller.Wear wear = inventoryWearRoller.roll(chosen.exterior());

        UserInventoryItem upgraded = new UserInventoryItem();
        upgraded.setUserId(userId);
        upgraded.setItemId(chosen.item().getId());
        upgraded.setExterior(chosen.exterior());
        upgraded.setStatTrack(statTrack);
        upgraded.setFloatValue(wear.floatValue());
        upgraded.setPaintSeed(wear.paintSeed());
        upgraded = userInventoryItemRepository.save(upgraded);

        return new SkinUpgradeSuccessResponse(
                upgraded.getId(),
                chosen.item().getCode(),
                chosen.item().getDisplayName(),
                chosen.item().getRarity(),
                chosen.exterior(),
                statTrack,
                wear.floatValue(),
                wear.paintSeed(),
                price
        );
    }

    private UpgradeTarget resolveChosenTarget(
            List<UpgradeTarget> targets,
            SkinUpgradeRequest request,
            Long inventoryItemId
    ) {
        if (request == null || request.targetItemId() == null || request.targetExterior() == null) {
            throw new SkinUpgradeInvalidTargetException(inventoryItemId);
        }
        return targets.stream()
                .filter(t -> t.item().getId().equals(request.targetItemId())
                        && t.exterior() == request.targetExterior())
                .findFirst()
                .orElseThrow(() -> new SkinUpgradeInvalidTargetException(inventoryItemId));
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

    private List<UpgradeTarget> findDearerTargets(BigDecimal currentPrice, boolean sourceStatTrack) {
        BigDecimal maxPrice = currentPrice
                .multiply(MAX_PRICE_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);

        List<UpgradeTarget> targets = new ArrayList<>();
        for (Item item : itemRepository.findByActiveTrueOrderByIdAsc()) {
            for (ItemExterior exterior : ItemExterior.values()) {
                BigDecimal price = SellPricing.sellPrice(
                        item.getRarity(), exterior, sourceStatTrack);
                if (price.compareTo(currentPrice) > 0 && price.compareTo(maxPrice) <= 0) {
                    targets.add(new UpgradeTarget(item, exterior, sourceStatTrack, price));
                }
            }
        }
        return targets;
    }

    private static SkinUpgradeTargetDto toDto(UpgradeTarget target) {
        Item item = target.item();
        return new SkinUpgradeTargetDto(
                item.getId(),
                item.getCode(),
                item.getDisplayName(),
                item.getRarity(),
                target.exterior(),
                target.statTrack(),
                target.price()
        );
    }

    private record UpgradeTarget(
            Item item,
            ItemExterior exterior,
            boolean statTrack,
            BigDecimal price
    ) {}
}
