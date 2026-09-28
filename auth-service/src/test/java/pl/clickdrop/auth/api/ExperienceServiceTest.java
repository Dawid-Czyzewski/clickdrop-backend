package pl.clickdrop.auth.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.auth.api.dto.ExperienceResponse;
import pl.clickdrop.auth.application.ExperienceService;
import pl.clickdrop.auth.application.exception.UserNotFoundException;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperienceServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ExperienceService experienceService;

    @Test
    void addExperience_increasesXp_withoutLevelUp() {
        User user = user(15L, 1, 50);
        when(userRepository.findById(15L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ExperienceResponse res = experienceService.addExperience(15L, 10);

        assertThat(res.experienceGained()).isEqualTo(10);
        assertThat(res.experience()).isEqualTo(60);
        assertThat(res.level()).isEqualTo(1);
        assertThat(res.leveledUp()).isFalse();
        assertThat(user.getExperience()).isEqualTo(60);
        assertThat(user.getLevel()).isEqualTo(1);
        verify(userRepository).save(user);
    }

    @Test
    void addExperience_levelsUp_whenCrossingThreshold() {
        User user = user(15L, 1, 95);
        when(userRepository.findById(15L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ExperienceResponse res = experienceService.addExperience(15L, 10);

        assertThat(res.experienceGained()).isEqualTo(10);
        assertThat(res.experience()).isEqualTo(105);
        assertThat(res.level()).isEqualTo(2); // 1 + 105/100
        assertThat(res.leveledUp()).isTrue();
        assertThat(user.getExperience()).isEqualTo(105);
        assertThat(user.getLevel()).isEqualTo(2);
        verify(userRepository).save(user);
    }

    @Test
    void addExperience_canGainMultipleLevelsInOneCall() {
        User user = user(15L, 1, 0);
        when(userRepository.findById(15L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ExperienceResponse res = experienceService.addExperience(15L, 250);

        assertThat(res.experience()).isEqualTo(250);
        assertThat(res.level()).isEqualTo(3); // 1 + 250/100
        assertThat(res.leveledUp()).isTrue();
    }

    @Test
    void addExperience_throws_whenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> experienceService.addExperience(99L, 10))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void addExperience_throws_whenAmountInvalid() {
        assertThatThrownBy(() -> experienceService.addExperience(15L, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> experienceService.addExperience(15L, -5))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    private static User user(Long id, int level, int experience) {
        User user = new User();
        user.setId(id);
        user.setLevel(level);
        user.setExperience(experience);
        return user;
    }
}