package pl.clickdrop.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.auth.api.dto.ExperienceResponse;
import pl.clickdrop.auth.application.exception.UserNotFoundException;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private static final int XP_PER_LEVEL = 100;

    private final UserRepository userRepository;

    @Transactional
    public ExperienceResponse addExperience(Long userId, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 1");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int previousLevel = user.getLevel();
        int newExperience = user.getExperience() + amount;
        int nextLevel = 1 + (newExperience / XP_PER_LEVEL);

        user.setExperience(newExperience);
        user.setLevel(nextLevel);
        userRepository.save(user);

        return new ExperienceResponse(
                nextLevel,
                newExperience,
                amount,
                nextLevel > previousLevel
        );
    }
}
