package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import pl.clickdrop.economy.application.exception.ClickRateLimitException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClickRateLimiterTest {

    private final ClickRateLimiter limiter = new ClickRateLimiter();

    @Test
    void allowsFirstClick() {
        assertThatCode(() -> limiter.check(1L)).doesNotThrowAnyException();
    }

    @Test
    void blocksImmediateSecondClick_forSameUser() {
        limiter.check(1L);

        assertThatThrownBy(() -> limiter.check(1L))
                .isInstanceOf(ClickRateLimitException.class);
    }

    @Test
    void allowsClicks_forDifferentUsersIndependently() {
        limiter.check(1L);

        assertThatCode(() -> limiter.check(2L)).doesNotThrowAnyException();
    }
}