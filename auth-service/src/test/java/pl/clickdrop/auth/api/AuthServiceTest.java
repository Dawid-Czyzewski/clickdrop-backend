package pl.clickdrop.auth.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.clickdrop.auth.api.dto.LoginRequest;
import pl.clickdrop.auth.api.dto.LoginResponse;
import pl.clickdrop.auth.application.AuthService;
import pl.clickdrop.auth.application.exception.InvalidCredentialsException;
import pl.clickdrop.auth.application.security.JwtService;
import pl.clickdrop.auth.domain.user.Role;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthService authService;

    @Test
    void login_returnsUserAndToken_whenCredentialsValid() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@clickdrop.pl");
        user.setPassword("$2a$10$hashed");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("test@clickdrop.pl")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "$2a$10$hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse response = authService.login(
                new LoginRequest("test@clickdrop.pl", "Secret123!")
        );

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@clickdrop.pl");
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void login_throws_whenEmailUnknown() {
        when(userRepository.findByEmail("brak@clickdrop.pl")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("brak@clickdrop.pl", "Secret123!"))
        ).isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_throws_whenPasswordWrong() {
        User user = new User();
        user.setEmail("test@clickdrop.pl");
        user.setPassword("$2a$10$hashed");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("test@clickdrop.pl")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("zle", "$2a$10$hashed")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("test@clickdrop.pl", "zle"))
        ).isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(jwtService);
    }
}