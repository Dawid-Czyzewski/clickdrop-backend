package pl.clickdrop.auth.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.auth.api.dto.UserResponse;
import pl.clickdrop.auth.application.UserService;
import pl.clickdrop.auth.application.exception.UserNotFoundException;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void getCurrentUser_returnsProfile() {
        User user = new User();
        user.setUsername("player");
        user.setEmail("player@clickdrop.pl");
        user.setLevel(5);
        user.setExperience(1200);

        when(userRepository.findByEmail("player@clickdrop.pl")).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUser("player@clickdrop.pl");

        assertThat(response.username()).isEqualTo("player");
        assertThat(response.email()).isEqualTo("player@clickdrop.pl");
        assertThat(response.level()).isEqualTo(5);
        assertThat(response.experience()).isEqualTo(1200);
    }

    @Test
    void getCurrentUser_throws_whenUserMissing() {
        when(userRepository.findByEmail("missing@clickdrop.pl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser("missing@clickdrop.pl"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getLevel_returnsUserLevel() {
        User user = new User();
        user.setId(15L);
        user.setLevel(7);

        when(userRepository.findById(15L)).thenReturn(Optional.of(user));

        assertThat(userService.getLevel(15L)).isEqualTo(7);
    }

    @Test
    void getLevel_throws_whenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getLevel(99L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
