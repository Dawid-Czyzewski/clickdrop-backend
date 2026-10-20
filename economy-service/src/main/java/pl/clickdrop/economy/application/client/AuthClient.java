package pl.clickdrop.economy.application.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthClient {

    private final RestClient authRestClient;

    public AuthExperienceResponse addExperience(Long userId, int amount) {
        return authRestClient.post()
                .uri("/api/internal/users/{userId}/experience", userId)
                .body(Map.of("amount", amount))
                .retrieve()
                .body(AuthExperienceResponse.class);
    }

    public int getLevel(Long userId) {
        AuthUserLevelResponse body = authRestClient.get()
                .uri("/api/internal/users/{userId}/level", userId)
                .retrieve()
                .body(AuthUserLevelResponse.class);
        if (body == null || body.level() == null) {
            throw new IllegalStateException("Auth level response was null for userId=" + userId);
        }
        return body.level();
    }
}

