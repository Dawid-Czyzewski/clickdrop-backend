package pl.clickdrop.auth.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.clickdrop.auth.api.dto.RefreshTokenRequest;
import pl.clickdrop.auth.api.dto.TokenResponse;
import pl.clickdrop.auth.application.AuthService;
import pl.clickdrop.auth.application.PasswordResetTokenService;
import pl.clickdrop.auth.application.client.EconomyClient;
import pl.clickdrop.auth.application.exception.InvalidRefreshTokenException;
import pl.clickdrop.auth.application.mail.EmailService;
import pl.clickdrop.auth.application.security.JwtService;
import pl.clickdrop.auth.application.security.RefreshTokenService;
import pl.clickdrop.auth.application.verification.EmailVerificationTokenService;
import pl.clickdrop.auth.domain.token.RefreshToken;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock EmailVerificationTokenService emailVerificationTokenService;
    @Mock EmailService emailService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock EconomyClient economyClient;
    @Mock PasswordResetTokenService passwordResetTokenService;

    @InjectMocks
    AuthService authService;

    @Test
    void refresh_rotatesTokens_whenRefreshValid() {
        User user = new User();
        user.setId(1L);
        user.setEmail("player@clickdrop.pl");

        RefreshToken existing = new RefreshToken();
        existing.setToken("old-refresh");
        existing.setUser(user);

        RefreshToken rotated = new RefreshToken();
        rotated.setToken("new-refresh");

        when(refreshTokenService.verify("old-refresh")).thenReturn(existing);
        when(refreshTokenService.createFor(user)).thenReturn(rotated);
        when(jwtService.generateToken(user)).thenReturn("new-access");

        TokenResponse response = authService.refresh(new RefreshTokenRequest("old-refresh"));

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenService).revoke("old-refresh");
        verify(refreshTokenService).createFor(user);
    }

    @Test
    void refresh_throws_whenRefreshTokenRevoked() {
        when(refreshTokenService.verify("revoked-refresh"))
                .thenThrow(new InvalidRefreshTokenException());

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("revoked-refresh")))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenService, never()).revoke(any());
        verify(refreshTokenService, never()).createFor(any());
        verify(jwtService, never()).generateToken(any());
    }
}
