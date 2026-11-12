package pl.clickdrop.economy.application;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TradeUpRng {

    public <T> T pickOne(List<T> options) {
        int i = ThreadLocalRandom.current().nextInt(options.size());
        return options.get(i);
    }
}
