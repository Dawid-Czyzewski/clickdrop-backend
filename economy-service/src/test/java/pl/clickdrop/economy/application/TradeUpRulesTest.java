package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import pl.clickdrop.economy.application.exception.TradeUpInvalidException;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeUpRulesTest {

    @Test
    void requireExactUniqueIds_rejectsWrongCountAndDuplicates() {
        assertThatThrownBy(() -> TradeUpRules.requireExactUniqueIds(List.of(1L, 2L)))
                .isInstanceOf(TradeUpInvalidException.class);

        List<Long> dupes = IntStream.rangeClosed(1, 9).mapToObj(i -> (long) i).toList();
        List<Long> withDupe = new java.util.ArrayList<>(dupes);
        withDupe.add(1L);
        assertThatThrownBy(() -> TradeUpRules.requireExactUniqueIds(withDupe))
                .isInstanceOf(TradeUpInvalidException.class);
    }

    @Test
    void nextRarity_stepsUpUntilCovert() {
        assertThat(TradeUpRules.nextRarity(ItemRarity.MIL_SPEC)).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(TradeUpRules.nextRarity(ItemRarity.RESTRICTED)).isEqualTo(ItemRarity.CLASSIFIED);
        assertThat(TradeUpRules.nextRarity(ItemRarity.CLASSIFIED)).isEqualTo(ItemRarity.COVERT);
        assertThat(TradeUpRules.nextRarity(ItemRarity.COVERT)).isNull();
    }

    @Test
    void averageExterior_roundsMeanOrdinal() {
        List<ItemExterior> allFn = Collections.nCopies(10, ItemExterior.FACTORY_NEW);
        assertThat(TradeUpRules.averageExterior(allFn)).isEqualTo(ItemExterior.FACTORY_NEW);

        List<ItemExterior> mixed = List.of(
                ItemExterior.FACTORY_NEW,
                ItemExterior.FACTORY_NEW,
                ItemExterior.FACTORY_NEW,
                ItemExterior.FACTORY_NEW,
                ItemExterior.FACTORY_NEW,
                ItemExterior.BATTLE_SCARRED,
                ItemExterior.BATTLE_SCARRED,
                ItemExterior.BATTLE_SCARRED,
                ItemExterior.BATTLE_SCARRED,
                ItemExterior.BATTLE_SCARRED
        );

        assertThat(TradeUpRules.averageExterior(mixed)).isEqualTo(ItemExterior.FIELD_TESTED);
    }

    @Test
    void resultStatTrack_onlyWhenAllTrue() {
        List<Boolean> allSt = Collections.nCopies(10, true);
        assertThat(TradeUpRules.resultStatTrack(allSt)).isTrue();

        List<Boolean> mixed = new java.util.ArrayList<>(Collections.nCopies(9, true));
        mixed.add(false);
        assertThat(TradeUpRules.resultStatTrack(mixed)).isFalse();
    }
}
