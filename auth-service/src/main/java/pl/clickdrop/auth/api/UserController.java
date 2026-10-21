package pl.clickdrop.auth.api;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.auth.api.dto.LeaderboardEntryResponse;
import pl.clickdrop.auth.api.dto.UserResponse;
import pl.clickdrop.auth.application.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getCurrentUser(email));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> leaderboard(
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(userService.leaderboard(limit));
    }

    @GetMapping("/leaderboard/me")
    public ResponseEntity<LeaderboardEntryResponse> myLeaderBoardRank(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                userService.myLeaderBoardRank(authentication.getName())
        );
    }
}
