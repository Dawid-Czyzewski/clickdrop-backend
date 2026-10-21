package pl.clickdrop.auth.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.auth.api.dto.LeaderboardEntryResponse;
import pl.clickdrop.auth.application.UserService;
import pl.clickdrop.auth.application.exception.UserNotFoundException;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceMyLeaderboardRankTest {

    @Mock UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void myLeaderBoardRank_isFirst_whenNobodyAbove() {
        User me = user("ace", "ace@clickdrop.pl", 12, 340);

        when(userRepository.findByEmail("ace@clickdrop.pl")).thenReturn(Optional.of(me));
        when(userRepository.countRankedAbove(12, 340)).thenReturn(0L);

        LeaderboardEntryResponse res = userService.myLeaderBoardRank("ace@clickdrop.pl");

        assertThat(res.rank()).isEqualTo(1);
        assertThat(res.username()).isEqualTo("ace");
        assertThat(res.level()).isEqualTo(12);
        assertThat(res.experience()).isEqualTo(340);
        verify(userRepository).countRankedAbove(12, 340);
    }

    @Test
    void myLeaderBoardRank_isMiddle_whenTwoAbove() {
        User me = user("nova", "nova@clickdrop.pl", 10, 50);

        when(userRepository.findByEmail("nova@clickdrop.pl")).thenReturn(Optional.of(me));
        when(userRepository.countRankedAbove(10, 50)).thenReturn(2L);

        LeaderboardEntryResponse res = userService.myLeaderBoardRank("nova@clickdrop.pl");

        assertThat(res.rank()).isEqualTo(3);
        assertThat(res.username()).isEqualTo("nova");
        assertThat(res.level()).isEqualTo(10);
        assertThat(res.experience()).isEqualTo(50);
    }

    @Test
    void myLeaderBoardRank_isOne_whenOnlyUser() {
        User me = user("solo", "solo@clickdrop.pl", 1, 0);

        when(userRepository.findByEmail("solo@clickdrop.pl")).thenReturn(Optional.of(me));
        when(userRepository.countRankedAbove(1, 0)).thenReturn(0L);

        assertThat(userService.myLeaderBoardRank("solo@clickdrop.pl").rank()).isEqualTo(1);
    }

    @Test
    void myLeaderBoardRank_usesDefaults_whenLevelOrXpNull() {
        User me = user("raw", "raw@clickdrop.pl", null, null);

        when(userRepository.findByEmail("raw@clickdrop.pl")).thenReturn(Optional.of(me));
        when(userRepository.countRankedAbove(1, 0)).thenReturn(4L);

        LeaderboardEntryResponse res = userService.myLeaderBoardRank("raw@clickdrop.pl");

        assertThat(res.rank()).isEqualTo(5);
        assertThat(res.level()).isEqualTo(1);
        assertThat(res.experience()).isEqualTo(0);
        verify(userRepository).countRankedAbove(1, 0);
    }

    @Test
    void myLeaderBoardRank_throws_whenUserMissing() {
        when(userRepository.findByEmail("missing@clickdrop.pl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.myLeaderBoardRank("missing@clickdrop.pl"))
                .isInstanceOf(UserNotFoundException.class);
    }

    private static User user(String username, String email, Integer level, Integer experience) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setLevel(level);
        u.setExperience(experience);
        u.setEnabled(true);
        return u;
    }
}
