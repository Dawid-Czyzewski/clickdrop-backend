package pl.clickdrop.auth.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        User user = new User();
        user.setEmail("test@clickdrop.pl");
        user.setUsername("tester");
        user.setPassword("{noop}Secret123!");
        user.setRole(Role.USER);
        user.setExperience(0);
        user.setLevel(1);
        user.setDollars(BigDecimal.ZERO);

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        User found = userRepository.findByEmail("test@clickdrop.pl").orElseThrow();

        assertThat(found.getUsername()).isEqualTo("tester");
        assertThat(found.getRole()).isEqualTo(Role.USER);
        assertThat(found.getExperience()).isZero();
        assertThat(found.getLevel()).isEqualTo(1);
        assertThat(found.getDollars()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(userRepository.existsByEmail("test@clickdrop.pl")).isTrue();
    }
}
