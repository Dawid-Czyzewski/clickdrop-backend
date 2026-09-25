package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import pl.clickdrop.economy.application.exception.CaseOpenRateLimitException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseOpenRateLimiterTest {

    private final CaseOpenRateLimiter limiter = new CaseOpenRateLimiter();

    @Test
    void allowsFirstOpen() {
        assertThatCode(() -> limiter.check(1L)).doesNotThrowAnyException();
    }

    @Test
    void blocksImmediateSecondOpen_forSameUser() {
        limiter.check(1L);

        assertThatThrownBy(() -> limiter.check(1L))
                .isInstanceOf(CaseOpenRateLimitException.class);
    }

    @Test
    void allowsOpens_forDifferentUsersIndependently() {
        limiter.check(1L);

        assertThatCode(() -> limiter.check(2L)).doesNotThrowAnyException();
    }
}