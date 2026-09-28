package pl.clickdrop.auth.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.auth.api.dto.AddExperienceRequest;
import pl.clickdrop.auth.api.dto.ExperienceResponse;
import pl.clickdrop.auth.application.ExperienceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private final ExperienceService experienceService;

    public ResponseEntity<ExperienceResponse> addExperience(
            @PathVariable Long userId,
            @Valid @RequestBody AddExperienceRequest request
    ) {
        return ResponseEntity.ok(
                experienceService.addExperience(userId, request.amount())
        );
    }
}
