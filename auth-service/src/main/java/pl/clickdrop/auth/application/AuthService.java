package pl.clickdrop.auth.application;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.clickdrop.auth.api.dto.*;
import pl.clickdrop.auth.application.client.EconomyClient;
import pl.clickdrop.auth.application.exception.*;
import pl.clickdrop.auth.application.mail.EmailService;
import pl.clickdrop.auth.application.security.JwtService;
import pl.clickdrop.auth.application.security.RefreshTokenService;
import pl.clickdrop.auth.application.verification.EmailVerificationTokenService;
import pl.clickdrop.auth.domain.token.RefreshToken;
import pl.clickdrop.auth.domain.user.Role;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;
import pl.clickdrop.auth.domain.verification.EmailVerificationToken;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final EconomyClient economyClient;

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email()).
                orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            throw new AccountNotEnabledException();
        }

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createFor(user);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                accessToken,
                refreshToken.getToken()
        );
    }

    public RegisterResponse register(RegisterRequest request) throws MessagingException {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException();
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setLevel(1);
        user.setEnabled(false);
        user.setExperience(0);

        User saved = userRepository.save(user);

        economyClient.createWallet(saved.getId());

        EmailVerificationToken verificationToken = emailVerificationTokenService.createFor(saved);

        emailService.sendAccountActivation(
                saved.getEmail(),
                saved.getUsername(),
                verificationToken.getToken()
        );

        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getUsername(), saved.getRole());
    }

    public void verifyEmail(String token) {
        emailVerificationTokenService.verify(token);
    }

    public void resendVerification(ResendVerificationRequest request) throws MessagingException {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        if (user.isEnabled()) {
            throw new AccountAlreadyVerifiedException();
        }

        emailVerificationTokenService.invalidateActiveTokens(user);
        EmailVerificationToken token = emailVerificationTokenService.createFor(user);

        emailService.sendAccountActivation(
                user.getEmail(),
                user.getUsername(),
                token.getToken()
        );
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken existing = refreshTokenService.verify(request.refreshToken());
        User user = existing.getUser();

        refreshTokenService.revoke(request.refreshToken());
        RefreshToken newRefresh = refreshTokenService.createFor(user);
        String accessToken = jwtService.generateToken(user);

        return new TokenResponse(accessToken, newRefresh.getToken());
    }

    public void revokeRefreshToken(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }
}
