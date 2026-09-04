package pl.clickdrop.auth.application.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.clickdrop.auth.config.JwtProperties;
import pl.clickdrop.auth.domain.user.Role;
import pl.clickdrop.auth.domain.user.User;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(
                "testtesttesttestetsteststetstestest",
                3_600_000L,
                3_600_000L
        );
        jwtService = new JwtService(props);
    }

    @Test
    void generateToken_containsSubjectAndClaims() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@clickdrop.pl");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();

        Claims claims = jwtService.parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("test@clickdrop.pl");
        assertThat(claims.get("id", Long.class)).isEqualTo(1L);
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalse_forTamperedToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@clickdrop.pl");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isValid(token + "x")).isFalse();
    }

    @Test
    void isValid_returnsFalse_forGarbage() {
        assertThat(jwtService.isValid("not-a-jwt")).isFalse();
    }
}