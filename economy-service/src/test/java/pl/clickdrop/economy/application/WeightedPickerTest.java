package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightedPickerTest {

    private final WeightedPicker picker = new WeightedPicker();

    private record Option(String name, int weight) {
    }

    @Test
    void pick_returnsOnlyOption_whenSingleEntry() {
        Option only = new Option("AK", 100);

        Option result = picker.pick(List.of(only), Option::weight);

        assertThat(result).isSameAs(only);
    }

    @Test
    void pick_throws_whenEmpty() {
        assertThatThrownBy(() -> picker.pick(List.<Option>of(), Option::weight))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pick_throws_whenTotalWeightZero() {
        assertThatThrownBy(() -> picker.pick(List.of(new Option("X", 0)), Option::weight))
                .isInstanceOf(IllegalArgumentException.class);
    }
}