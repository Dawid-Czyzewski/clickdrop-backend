package pl.clickdrop.auth.verification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.auth.application.verification.EmailVerificationTokenService;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.verification.EmailVerificationToken;
import pl.clickdrop.auth.domain.verification.EmailVerificationTokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationTokenServiceTest {

    @Mock
    EmailVerificationTokenRepository repository;

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
}