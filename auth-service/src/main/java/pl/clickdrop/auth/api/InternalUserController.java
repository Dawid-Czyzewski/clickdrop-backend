package pl.clickdrop.auth.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.clickdrop.auth.api.dto.AddExperienceRequest;
import pl.clickdrop.auth.api.dto.ExperienceResponse;
import pl.clickdrop.auth.api.dto.UserLevelResponse;
import pl.clickdrop.auth.application.ExperienceService;
import pl.clickdrop.auth.application.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private final ExperienceService experienceService;
    private final UserService userService;

    @PostMapping("/{userId}/experience")
    public ResponseEntity<ExperienceResponse> addExperience(
            @PathVariable Long userId,
            @Valid @RequestBody AddExperienceRequest request
    ) {
        return ResponseEntity.ok(
                experienceService.addExperience(userId, request.amount())
        );
    }

    @GetMapping("/{userId}/level")
    public ResponseEntity<UserLevelResponse> getLevel(@PathVariable Long userId) {
        return ResponseEntity.ok(new UserLevelResponse(userService.getLevel(userId)));
    }
}
