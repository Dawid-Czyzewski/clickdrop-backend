package pl.clickdrop.auth.domain.passwordreset;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.clickdrop.auth.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findAllByUserAtIsNull(User user);
}
