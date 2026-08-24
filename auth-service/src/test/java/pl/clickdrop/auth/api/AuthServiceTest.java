package pl.clickdrop.auth.api;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.clickdrop.auth.api.dto.LoginRequest;
import pl.clickdrop.auth.api.dto.LoginResponse;
import pl.clickdrop.auth.api.dto.RegisterRequest;
import pl.clickdrop.auth.api.dto.RegisterResponse;
import pl.clickdrop.auth.application.AuthService;
import pl.clickdrop.auth.application.exception.AccountNotEnabledException;
import pl.clickdrop.auth.application.exception.EmailAlreadyExistsException;
import pl.clickdrop.auth.application.exception.InvalidCredentialsException;
import pl.clickdrop.auth.application.exception.UsernameAlreadyExistsException;
import pl.clickdrop.auth.application.mail.EmailService;
import pl.clickdrop.auth.application.security.JwtService;
import pl.clickdrop.auth.application.verification.EmailVerificationTokenService;
import pl.clickdrop.auth.domain.user.Role;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;
import pl.clickdrop.auth.domain.verification.EmailVerificationToken;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @Mock
    EmailVerificationTokenService emailVerificationTokenService;

    @InjectMocks
    AuthService authService;

    @Mock
    EmailService emailService;

    @Test
    void login_returnsUserAndToken_whenCredentialsValid() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@clickdrop.pl");
        user.setPassword("$2a$10$hashed");
        user.setRole(Role.USER);
        user.setEnabled(true);

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
        when(userRepository.findByEmail("unknown@clickdrop.pl")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("unknown@clickdrop.pl", "Secret123!"))
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
        when(passwordEncoder.matches("wrong", "$2a$10$hashed")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("test@clickdrop.pl", "wrong"))
        ).isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(jwtService);
    }

    @Test
    void register_createsUser_whenDataValid() throws MessagingException {
        RegisterRequest request = new RegisterRequest(
                "new@clickdrop.pl",
                "newuser",
                "Secret123!"
        );

        when(userRepository.existsByEmail("new@clickdrop.pl")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("$2a$10$hashed");

        User saved = new User();
        saved.setId(10L);
        saved.setEmail("new@clickdrop.pl");
        saved.setUsername("newuser");
        saved.setPassword("$2a$10$hashed");
        saved.setRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(saved);

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken("activation-token");
        when(emailVerificationTokenService.createFor(saved)).thenReturn(verificationToken);

        RegisterResponse response = authService.register(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo("new@clickdrop.pl");
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.role()).isEqualTo(Role.USER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User toSave = captor.getValue();
        assertThat(toSave.getEmail()).isEqualTo("new@clickdrop.pl");
        assertThat(toSave.getUsername()).isEqualTo("newuser");
        assertThat(toSave.getPassword()).isEqualTo("$2a$10$hashed");
        assertThat(toSave.getRole()).isEqualTo(Role.USER);
        assertThat(toSave.getExperience()).isZero();
        assertThat(toSave.getLevel()).isEqualTo(1);
        assertThat(toSave.getDollars()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(emailVerificationTokenService).createFor(saved);

        verify(passwordEncoder).encode("Secret123!");
        verifyNoInteractions(jwtService);

        verify(emailService).sendAccountActivation(
                "new@clickdrop.pl",
                "newuser",
                "activation-token"
        );
    }

    @Test
    void register_throws_whenEmailExists() {
        when(userRepository.existsByEmail("taken@clickdrop.pl")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest("taken@clickdrop.pl", "user1", "Secret123!"))
        ).isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void register_throws_whenUsernameExists() {
        when(userRepository.existsByEmail("ok@clickdrop.pl")).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest("ok@clickdrop.pl", "taken", "Secret123!"))
        ).isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_throws_whenAccountNotEnabled() {
        User user = new User();
        user.setEmail("test@clickdrop.pl");
        user.setPassword("$2a$10$hashed");
        user.setRole(Role.USER);
        user.setEnabled(false);

        when(userRepository.findByEmail("test@clickdrop.pl")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "$2a$10$hashed")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("test@clickdrop.pl", "Secret123!"))
        ).isInstanceOf(AccountNotEnabledException.class);

        verifyNoInteractions(jwtService);
    }
    @Test
    void register_setsEnabledToFalse() throws MessagingException {
        RegisterRequest request = new RegisterRequest(
                "new@clickdrop.pl",
                "newuser",
                "Secret123!"
        );

        when(userRepository.existsByEmail("new@clickdrop.pl")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("$2a$10$hashed");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        authService.register(request);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isEnabled()).isFalse();
    }
}