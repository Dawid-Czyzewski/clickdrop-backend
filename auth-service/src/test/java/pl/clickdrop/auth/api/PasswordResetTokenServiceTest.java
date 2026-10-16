package pl.clickdrop.auth.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.auth.application.PasswordResetTokenService;
import pl.clickdrop.auth.application.exception.ExpiredPasswordResetTokenException;
import pl.clickdrop.auth.application.exception.InvalidPasswordResetTokenException;
import pl.clickdrop.auth.domain.passwordreset.PasswordResetToken;
import pl.clickdrop.auth.domain.passwordreset.PasswordResetTokenRepository;
import pl.clickdrop.auth.domain.user.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {

    @Mock
    PasswordResetTokenRepository repository;

    @InjectMocks
    PasswordResetTokenService service;

    @Test
    void createFor_savesUniqueTokenLinkedToUser() {
        User user = new User();
        user.setId(1L);

        when(repository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetToken result = service.createFor(user);

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(repository).save(captor.capture());

        PasswordResetToken saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getToken())
                .isNotBlank()
                .hasSize(32)
                .doesNotContain("-");
        assertThat(saved.getExpirationDate())
                .isCloseTo(Instant.now().plus(1, ChronoUnit.HOURS), within(5, ChronoUnit.SECONDS));
        assertThat(result.getToken()).isEqualTo(saved.getToken());
    }

    @Test
    void consume_marksTokenUsed_andReturnsUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("player@clickdrop.pl");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpirationDate(Instant.now().plus(1, ChronoUnit.HOURS));
        token.setUsedAt(null);

        when(repository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(repository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.consume("valid-token");

        assertThat(result).isSameAs(user);
        assertThat(token.getUsedAt()).isNotNull();
        verify(repository).save(token);
    }

    @Test
    void consume_throws_whenTokenUnknown() {
        when(repository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consume("missing"))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void consume_throws_whenTokenAlreadyUsed() {
        User user = new User();
        user.setId(1L);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("used-token");
        token.setUser(user);
        token.setExpirationDate(Instant.now().plus(1, ChronoUnit.HOURS));
        token.setUsedAt(Instant.now().minus(1, ChronoUnit.MINUTES));

        when(repository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.consume("used-token"))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void consume_throws_whenTokenExpired() {
        User user = new User();
        user.setId(1L);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expired-token");
        token.setUser(user);
        token.setExpirationDate(Instant.now().minus(1, ChronoUnit.MINUTES));
        token.setUsedAt(null);

        when(repository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.consume("expired-token"))
                .isInstanceOf(ExpiredPasswordResetTokenException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void invalidateActiveTokens_marksUnusedTokensAsUsed() {
        User user = new User();
        user.setId(1L);

        PasswordResetToken active = new PasswordResetToken();
        active.setUser(user);
        active.setUsedAt(null);

        when(repository.findAllByUserAndUsedAtIsNull(user)).thenReturn(List.of(active));
        when(repository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        service.invalidateActiveTokens(user);

        assertThat(active.getUsedAt()).isNotNull();
        verify(repository).saveAll(List.of(active));
    }
}
