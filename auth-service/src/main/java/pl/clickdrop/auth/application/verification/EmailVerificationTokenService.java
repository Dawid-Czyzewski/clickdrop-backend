package pl.clickdrop.auth.application.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.verification.EmailVerificationToken;
import pl.clickdrop.auth.domain.verification.EmailVerificationTokenRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenService {

    private static final Duration TTL = Duration.ofHours(24);
    private final EmailVerificationTokenRepository repository;

    public EmailVerificationToken createFor(User user) {
        EmailVerificationToken entity = new EmailVerificationToken();
        entity.setUser(user);
        entity.setToken(generateToken());
        entity.setExpirationDate(Instant.now().plus(TTL));

        return repository.save(entity);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
