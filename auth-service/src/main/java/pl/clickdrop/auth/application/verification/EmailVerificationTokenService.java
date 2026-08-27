package pl.clickdrop.auth.application.verification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.clickdrop.auth.application.exception.ExpiredVerificationTokenException;
import pl.clickdrop.auth.application.exception.InvalidVerificationTokenException;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;
import pl.clickdrop.auth.domain.verification.EmailVerificationToken;
import pl.clickdrop.auth.domain.verification.EmailVerificationTokenRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenService {

    private static final Duration TTL = Duration.ofHours(24);
    private final EmailVerificationTokenRepository repository;
    private final UserRepository userRepository;

    public EmailVerificationToken createFor(User user) {
        EmailVerificationToken entity = new EmailVerificationToken();
        entity.setUser(user);
        entity.setToken(generateToken());
        entity.setExpirationDate(Instant.now().plus(TTL));

        return repository.save(entity);
    }

    @Transactional
    public void verify(String rawToken) {
        EmailVerificationToken token = repository.findByToken(rawToken)
                .orElseThrow(InvalidVerificationTokenException::new);

        User user = token.getUser();
        
        if (token.isUsed()) {
            if (user.isEnabled()) {
                return;
            }
            throw new InvalidVerificationTokenException();
        }

        if (token.isExpired()) {
            throw new ExpiredVerificationTokenException();
        }

        user.setEnabled(true);
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        repository.save(token);
    }

    public void invalidateActiveTokens(User user) {
        List<EmailVerificationToken> activeTokens = repository.findAllByUserAndUsedAtIsNull(user);
        Instant now = Instant.now();

        for (EmailVerificationToken token: activeTokens) {
            token.setUsedAt(now);
        }

        repository.saveAll(activeTokens);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
