package pl.clickdrop.economy.application;

import org.springframework.stereotype.Component;
import pl.clickdrop.economy.application.exception.CaseOpenRateLimitException;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaseOpenRateLimiter {

    private static  final Duration MIN_INTERVAL = Duration.ofMillis(500);
    private final ConcurrentHashMap<Long, Instant> lastOpenByUser = new ConcurrentHashMap<>();

    public void check(Long userId) {
        Instant now = Instant.now();

        lastOpenByUser.compute(userId, (id, last) -> {
            if (last != null && Duration.between(last, now).compareTo(MIN_INTERVAL) < 0) {
                throw new CaseOpenRateLimitException();
            }

            return now;
        });
    }
}
