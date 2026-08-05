package pl.clickdrop.auth.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.auth.api.dto.LoginRequest;
import pl.clickdrop.auth.api.dto.LoginResponse;
import pl.clickdrop.auth.application.AuthService;
import pl.clickdrop.auth.application.exception.InvalidCredentialsException;
import pl.clickdrop.auth.domain.user.Role;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthService authService;

    @Test
    void login_returnsUser_whenCredentialsValid() {
        User user = new User();
        user.setEmail("test@clickdrop.pl");
        user.setUsername("tester");
        user.setPassword("Secret123!");
        user.setRole(Role.USER);
        user.setExperience(0);
        user.setLevel(1);
        user.setDollars(BigDecimal.ZERO);

        when(userRepository.findByEmail("test@clickdrop.pl")).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(
            new LoginRequest("test@clickdrop.pl", "Secret123!")
        );

        assertThat(response.email()).isEqualTo("test@clickdrop.pl");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void login_throws_whenEmailUnknown() {
        when(userRepository.findByEmail("te@2.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("te@2.com", "sss"))
        ).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_whenPasswordWrong() {
        User user = new User();
        user.setEmail("test@clickdrop.pl");
        user.setUsername("tester");
        user.setPassword("Secret123!");
        user.setRole(Role.USER);
        user.setExperience(0);
        user.setLevel(1);
        user.setDollars(BigDecimal.ZERO);

        when(userRepository.findByEmail("test@clickdrop.pl")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> {
            authService.login(new LoginRequest("test@clickdrop.pl", "sss"));
        }).isInstanceOf(InvalidCredentialsException.class);
    }
}
