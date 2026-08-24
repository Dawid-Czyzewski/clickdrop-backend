package pl.clickdrop.auth.domain.verification;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.clickdrop.auth.domain.user.User;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUser(User user);
}
