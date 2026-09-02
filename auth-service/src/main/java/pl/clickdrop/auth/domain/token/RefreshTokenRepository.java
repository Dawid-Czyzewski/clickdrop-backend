package pl.clickdrop.auth.domain.token;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.clickdrop.auth.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUserAndRevokedAtIsNull(User user);
}
