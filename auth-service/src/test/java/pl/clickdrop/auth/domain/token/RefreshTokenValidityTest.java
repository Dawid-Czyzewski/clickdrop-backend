package pl.clickdrop.auth.domain.token;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenValidityTest {

    @Test
    void isValid_true_whenNotRevokedAndNotExpired() {
        RefreshToken token = new RefreshToken();
        token.setRevokedAt(null);
        token.setExpirationDate(Instant.now().plus(1, ChronoUnit.DAYS));

        assertThat(token.isRevoked()).isFalse();
        assertThat(token.isExpired()).isFalse();
        assertThat(token.isValid()).isTrue();
    }

    @Test
    void isValid_false_whenRevoked_evenIfNotExpired() {
        RefreshToken token = new RefreshToken();
        token.setRevokedAt(Instant.now());
        token.setExpirationDate(Instant.now().plus(1, ChronoUnit.DAYS));

        assertThat(token.isRevoked()).isTrue();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    void isValid_false_whenExpired_evenIfNotRevoked() {
        RefreshToken token = new RefreshToken();
        token.setRevokedAt(null);
        token.setExpirationDate(Instant.now().minus(1, ChronoUnit.MINUTES));

        assertThat(token.isExpired()).isTrue();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    void isValid_false_whenRevokedAndExpired() {
        RefreshToken token = new RefreshToken();
        token.setRevokedAt(Instant.now());
        token.setExpirationDate(Instant.now().minus(1, ChronoUnit.MINUTES));

        assertThat(token.isValid()).isFalse();
    }
}
