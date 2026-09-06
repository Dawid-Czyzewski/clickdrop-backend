package pl.clickdrop.economy.application;

import org.springframework.stereotype.Component;
import pl.clickdrop.economy.application.exception.ClickRateLimitException;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClickRateLimiter {

    private static final Duration MIN_INTERVAL = Duration.ofMillis(200);
    private final ConcurrentHashMap<Long, Instant> lastClickByUser = new ConcurrentHashMap<>();

    public void check(Long userId) {
        Instant now = Instant.now();

        lastClickByUser.compute(userId, (id, last) -> {
            if (last != null && Duration.between(last, now).compareTo(MIN_INTERVAL) < 0) {
                throw new ClickRateLimitException();
            }
            return now;
        });
    }
}
