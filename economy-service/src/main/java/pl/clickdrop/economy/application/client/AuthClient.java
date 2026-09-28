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
}
