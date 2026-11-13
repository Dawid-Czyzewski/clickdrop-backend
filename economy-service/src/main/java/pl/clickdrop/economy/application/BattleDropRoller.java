package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.clickdrop.economy.application.exception.CaseDropPoolEmptyException;
import pl.clickdrop.economy.domain.cases.CaseDrop;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BattleDropRoller {

    private final WeightedPicker weightedPicker;
    private final ExteriorRoller exteriorRoller;
    private final StatTrackRoller statTrackRoller;

    public record RolledDrop(
            Long itemId,
            Item item,
            ItemExterior exterior,
            boolean statTrack,
            BigDecimal value
    ) {}

    public RolledDrop roll(
            Long caseId,
            List<CaseDrop> pool,
            Map<Long, Item> itemsById
    ) {
        CaseDrop chosen = weightedPicker.pick(pool, CaseDrop::getWeight);
        Item item = itemsById.get(chosen.getItemId());
        if (item == null || !item.isActive()) {
            throw new CaseDropPoolEmptyException(caseId);
        }
        ItemExterior exterior = exteriorRoller.roll();
        boolean statTrack = statTrackRoller.roll();
        BigDecimal value = SellPricing.sellPrice(item.getRarity(), exterior, statTrack);
        return new RolledDrop(item.getId(), item, exterior, statTrack, value);
    }
}
