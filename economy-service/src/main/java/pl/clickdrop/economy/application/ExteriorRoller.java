package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.clickdrop.economy.domain.item.ItemExterior;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExteriorRoller {

    private record WeightedExterior(ItemExterior exterior, int weight) { }

    private static final List<WeightedExterior> POOL = List.of(
            new WeightedExterior(ItemExterior.FACTORY_NEW, 10),
            new WeightedExterior(ItemExterior.MINIMAL_WEAR, 20),
            new WeightedExterior(ItemExterior.FIELD_TESTED, 40),
            new WeightedExterior(ItemExterior.WELL_WORN, 20),
            new WeightedExterior(ItemExterior.BATTLE_SCARRED, 10)
    );

    private final WeightedPicker weightedPicker;

    public ItemExterior roll() {
        return weightedPicker.pick(POOL, WeightedExterior::weight).exterior();
    }
}
