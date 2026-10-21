package pl.clickdrop.auth.application;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.clickdrop.auth.api.dto.LeaderboardEntryResponse;
import pl.clickdrop.auth.api.dto.UserResponse;
import pl.clickdrop.auth.application.exception.UserNotFoundException;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    static final int DEFAULT_LIMIT = 20;
    static final int MAX_LIMIT = 100;

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        return toResponse(user);
    }

    public int getLevel(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return user.getLevel() == null ? 1 : user.getLevel();
    }

    public List<LeaderboardEntryResponse> leaderboard(Integer limit) {
        int size = normalizeLimit(limit);
        List<User> users = userRepository
                .findByEnabledTrueOrderByLevelDescExperienceDesc(PageRequest.of(0, size));

        List<LeaderboardEntryResponse> result = new ArrayList<>(users.size());
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            result.add(new LeaderboardEntryResponse(
               i + 1,
                    user.getUsername(),
                    user.getLevel(),
                    user.getExperience()
            ));
        }

        return result;
    }

    public LeaderboardEntryResponse myLeaderBoardRank(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        int level = user.getLevel() == null ? 1 : user.getLevel();
        int experience = user.getExperience() == null ? 0 : user.getExperience();
        int rank = (int) userRepository.countRankedAbove(level, experience) + 1;

        return new LeaderboardEntryResponse(
                rank,
                user.getUsername(),
                level,
                experience
        );
    }

    private static int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getEmail(),
                user.getLevel(),
                user.getExperience()
        );
    }
}
