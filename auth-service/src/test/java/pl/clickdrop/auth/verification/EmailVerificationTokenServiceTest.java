package pl.clickdrop.auth.verification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.auth.application.exception.ExpiredVerificationTokenException;
import pl.clickdrop.auth.application.exception.InvalidVerificationTokenException;
import pl.clickdrop.auth.application.verification.EmailVerificationTokenService;
import pl.clickdrop.auth.domain.user.Role;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;
import pl.clickdrop.auth.domain.verification.EmailVerificationToken;
import pl.clickdrop.auth.domain.verification.EmailVerificationTokenRepository;

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
class EmailVerificationTokenServiceTest {

    @Mock
    EmailVerificationTokenRepository repository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    EmailVerificationTokenService service;

    @Test
    void createFor_savesUniqueTokenLinkedToUser() {
        User user = new User();
        user.setId(1L);

        when(repository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmailVerificationToken result = service.createFor(user);

        ArgumentCaptor<EmailVerificationToken> captor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(repository).save(captor.capture());

        EmailVerificationToken saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getToken())
                .isNotBlank()
                .hasSize(32)
                .doesNotContain("-");
        assertThat(saved.getExpirationDate())
                .isCloseTo(Instant.now().plus(24, ChronoUnit.HOURS), within(5, ChronoUnit.SECONDS));
        assertThat(result.getToken()).isEqualTo(saved.getToken());
    }

    @Test
    void verify_enablesUser_andMarksTokenUsed() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@clickdrop.pl");
        user.setRole(Role.USER);
        user.setEnabled(false);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpirationDate(Instant.now().plus(1, ChronoUnit.HOURS));
        token.setUsedAt(null);

        when(repository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any(EmailVerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.verify("valid-token");

        assertThat(user.isEnabled()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();

        verify(userRepository).save(user);
        verify(repository).save(token);
    }

    @Test
    void verify_throws_whenTokenUnknown() {
        when(repository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify("missing"))
                .isInstanceOf(InvalidVerificationTokenException.class);

        verify(userRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void verify_throws_whenTokenAlreadyUsed() {
        User user = new User();
        user.setEnabled(false);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("used-token");
        token.setUser(user);
        token.setExpirationDate(Instant.now().plus(1, ChronoUnit.HOURS));
        token.setUsedAt(Instant.now().minus(1, ChronoUnit.MINUTES));

        when(repository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verify("used-token"))
                .isInstanceOf(InvalidVerificationTokenException.class);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void verify_throws_whenTokenExpired() {
        User user = new User();
        user.setEnabled(false);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("expired-token");
        token.setUser(user);
        token.setExpirationDate(Instant.now().minus(1, ChronoUnit.MINUTES));
        token.setUsedAt(null);

        when(repository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verify("expired-token"))
                .isInstanceOf(ExpiredVerificationTokenException.class);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void invalidateActiveTokens_marksUnusedTokensAsUsed() {
        User user = new User();
        user.setId(1L);

        EmailVerificationToken active = new EmailVerificationToken();
        active.setUser(user);
        active.setUsedAt(null);

        when(repository.findAllByUserAndUsedAtIsNull(user)).thenReturn(List.of(active));
        when(repository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        service.invalidateActiveTokens(user);

        assertThat(active.getUsedAt()).isNotNull();
        verify(repository).saveAll(List.of(active));
    }
}