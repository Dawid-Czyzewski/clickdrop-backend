package pl.clickdrop.auth.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.auth.application.exception.InvalidRefreshTokenException;
import pl.clickdrop.auth.application.security.RefreshTokenService;
import pl.clickdrop.auth.config.JwtProperties;
import pl.clickdrop.auth.domain.token.RefreshToken;
import pl.clickdrop.auth.domain.token.RefreshTokenRepository;
import pl.clickdrop.auth.domain.user.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    RefreshTokenRepository repository;

    RefreshTokenService service;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(
                "change-me-to-a-long-secret-at-least-32-chars!!",
                900_000L,
                604_800_000L
        );
        service = new RefreshTokenService(repository, props);
    }

    @Test
    void createFor_savesTokenLinkedToUser_withLongerExpiry() {
        User user = new User();
        user.setId(1L);

        when(repository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = service.createFor(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getToken()).isNotBlank().hasSize(32);
        assertThat(saved.getExpirationDate()).isAfter(Instant.now().plus(6, ChronoUnit.DAYS));
        assertThat(result.getToken()).isEqualTo(saved.getToken());
    }

    @Test
    void verify_returnsToken_whenValid() {
        User user = new User();
        RefreshToken token = new RefreshToken();
        token.setToken("valid");
        token.setUser(user);
        token.setExpirationDate(Instant.now().plus(1, ChronoUnit.DAYS));

        when(repository.findByToken("valid")).thenReturn(Optional.of(token));

        assertThat(service.verify("valid")).isSameAs(token);
    }

    @Test
    void verify_throws_whenExpired() {
        RefreshToken token = new RefreshToken();
        token.setToken("expired");
        token.setExpirationDate(Instant.now().minus(1, ChronoUnit.MINUTES));

        when(repository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verify("expired"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void revoke_setsRevokedAt() {
        RefreshToken token = new RefreshToken();
        token.setToken("to-revoke");
        token.setExpirationDate(Instant.now().plus(1, ChronoUnit.DAYS));

        when(repository.findByToken("to-revoke")).thenReturn(Optional.of(token));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.revoke("to-revoke");

        assertThat(token.getRevokedAt()).isNotNull();
        verify(repository).save(token);
    }
}