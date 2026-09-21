package pl.clickdrop.economy.application;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;

@Component
public class WeightedPicker {

    public <T> T pick(List<T> options, ToIntFunction<T> weightFn) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options must not be empty");
        }

        int total = options.stream().mapToInt(weightFn).sum();

        if (total <= 0) {
            throw new IllegalArgumentException("Total weight must be > 0");
        }

        int roll = ThreadLocalRandom.current().nextInt(total);
        int cursor = 0;

        for (T option : options) {
            cursor += weightFn.applyAsInt(option);

            if (roll < cursor) {
                return option;
            }
        }

        return options.getLast();
    }
}
