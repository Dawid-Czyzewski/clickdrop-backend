package pl.clickdrop.auth.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.clickdrop.auth.api.dto.*;
import pl.clickdrop.auth.application.client.EconomyClient;
import pl.clickdrop.auth.application.exception.*;
import pl.clickdrop.auth.application.mail.EmailService;
import pl.clickdrop.auth.application.security.JwtService;
import pl.clickdrop.auth.application.security.RefreshTokenService;
import pl.clickdrop.auth.application.verification.EmailVerificationTokenService;
import pl.clickdrop.auth.domain.passwordreset.PasswordResetToken;
import pl.clickdrop.auth.domain.token.RefreshToken;
import pl.clickdrop.auth.domain.user.Role;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;
import pl.clickdrop.auth.domain.verification.EmailVerificationToken;

@Slf4j
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
    private final PasswordResetTokenService passwordResetTokenService;

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

    public RegisterResponse register(RegisterRequest request) {
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
        sendActivationOrLog(saved, verificationToken.getToken());

        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getUsername(), saved.getRole());
    }

    public void verifyEmail(String token) {
        emailVerificationTokenService.verify(token);
    }

    public void resendVerification(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        if (user.isEnabled()) {
            throw new AccountAlreadyVerifiedException();
        }

        emailVerificationTokenService.invalidateActiveTokens(user);
        EmailVerificationToken token = emailVerificationTokenService.createFor(user);
        sendActivationOrLog(user, token.getToken());
    }

    private void sendActivationOrLog(User user, String rawToken) {
        try {
            emailService.sendAccountActivation(user.getEmail(), user.getUsername(), rawToken);
        } catch (Exception ex) {
            String link = emailService.buildActivationLink(rawToken);
            log.warn(
                    "Activation email failed for {}; continue registration. Verify via: {}",
                    user.getEmail(),
                    link,
                    ex
            );
        }
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

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        var userOpt = userRepository.findByEmail(request.email());

        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        passwordResetTokenService.invalidateActiveTokens(user);
        PasswordResetToken token = passwordResetTokenService.createFor(user);

        try {
            emailService.sendPasswordReset(
                    user.getEmail(),
                    user.getUsername(),
                    token.getToken()
            );
        } catch (Exception ex) {
            String link = emailService.buildPasswordResetLink(token.getToken());
            log.warn(
                    "Password-reset email failed for {}; token created. Reset via: {}",
                    user.getEmail(),
                    link,
                    ex
            );
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = passwordResetTokenService.consume(request.token());
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user);
    }

    @Transactional
    public void logoutAll(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        refreshTokenService.revokeAllForUser(user);
    }
}
