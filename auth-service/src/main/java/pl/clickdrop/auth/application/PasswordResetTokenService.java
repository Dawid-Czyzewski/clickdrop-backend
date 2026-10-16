package pl.clickdrop.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.auth.application.exception.ExpiredPasswordResetTokenException;
import pl.clickdrop.auth.application.exception.InvalidPasswordResetTokenException;
import pl.clickdrop.auth.domain.passwordreset.PasswordResetToken;
import pl.clickdrop.auth.domain.passwordreset.PasswordResetTokenRepository;
import pl.clickdrop.auth.domain.user.User;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private static final Duration TTL = Duration.ofHours(1);

    private final PasswordResetTokenRepository repository;

    public PasswordResetToken createFor(User user) {
        PasswordResetToken entity = new PasswordResetToken();
        entity.setUser(user);
        entity.setToken(generateToken());
        entity.setExpirationDate(Instant.now().plus(TTL));

        return repository.save(entity);
    }

    public void invalidateActiveTokens(User user) {
        List<PasswordResetToken> active = repository.findAllByUserAndUsedAtIsNull(user);
        Instant now = Instant.now();

        for (PasswordResetToken token : active) {
            token.setUsedAt(now);
        }

        repository.saveAll(active);
    }

    @Transactional
    public User consume(String rawToken) {
        PasswordResetToken token =repository.findByToken(rawToken)
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (token.isUsed()) {
            throw new InvalidPasswordResetTokenException();
        }

        if (token.isExpired()) {
            throw new ExpiredPasswordResetTokenException();
        }

        token.setUsedAt(Instant.now());
        repository.save(token);

        return token.getUser();
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
