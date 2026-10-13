package pl.clickdrop.auth.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import pl.clickdrop.auth.api.dto.LeaderboardEntryResponse;
import pl.clickdrop.auth.application.UserService;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceLeaderboardTest {

    @Mock UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void leaderboard_mapsRanks_andUsesDefaultLimit() {
        User ace = user("ace", 12, 340);
        User nova = user("nova", 12, 120);

        when(userRepository.findByEnabledTrueOrderByLevelDescExperienceDesc(any(Pageable.class)))
                .thenReturn(List.of(ace, nova));

        List<LeaderboardEntryResponse> result = userService.leaderboard(null);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByEnabledTrueOrderByLevelDescExperienceDesc(pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).username()).isEqualTo("ace");
        assertThat(result.get(0).level()).isEqualTo(12);
        assertThat(result.get(0).experience()).isEqualTo(340);
        assertThat(result.get(1).rank()).isEqualTo(2);
        assertThat(result.get(1).username()).isEqualTo("nova");
    }

    @Test
    void leaderboard_clampsLimitToMax() {
        when(userRepository.findByEnabledTrueOrderByLevelDescExperienceDesc(any(Pageable.class)))
                .thenReturn(List.of());

        userService.leaderboard(999);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByEnabledTrueOrderByLevelDescExperienceDesc(pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
    }

    private static User user(String username, int level, int experience) {
        User u = new User();
        u.setUsername(username);
        u.setLevel(level);
        u.setExperience(experience);
        u.setEnabled(true);
        return u;
    }
}