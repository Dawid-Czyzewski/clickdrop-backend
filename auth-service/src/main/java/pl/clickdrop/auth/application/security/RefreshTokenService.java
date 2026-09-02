package pl.clickdrop.auth.application.security;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.clickdrop.auth.application.exception.InvalidRefreshTokenException;
import pl.clickdrop.auth.config.JwtProperties;
import pl.clickdrop.auth.domain.token.RefreshToken;
import pl.clickdrop.auth.domain.token.RefreshTokenRepository;
import pl.clickdrop.auth.domain.user.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshToken createFor(User user) {
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setToken(generateToken());
        entity.setExpirationDate(
                Instant.now().plusMillis(jwtProperties.refreshExpirationMs())
        );

        return repository.save(entity);
    }

    public RefreshToken verify(String rawToken) {
        RefreshToken token = repository.findByToken(rawToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!token.isValid()) {
            throw new InvalidRefreshTokenException();
        }

        return token;
    }

    @Transactional
    public void revoke(String rawToken) {
        RefreshToken token = repository.findByToken(rawToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (token.isRevoked()) {
            return;
        }

        token.setRevokedAt(Instant.now());
        repository.save(token);
    }

    @Transactional
    public void revokeAllForUser(User user) {
        List<RefreshToken> active = repository.findAllByUserAndRevokedAtIsNull(user);
        Instant now = Instant.now();

        for (RefreshToken token : active) {
            token.setRevokedAt(now);
        }

        repository.saveAll(active);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
