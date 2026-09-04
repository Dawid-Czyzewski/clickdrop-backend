package pl.clickdrop.auth.application.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EconomyClient {

    private final RestClient economyRestClient;

    public void createWallet(Long userId) {
        economyRestClient.post()
                .uri("/api/economy/wallets")
                .body(Map.of("userId", userId))
                .retrieve()
                .toBodilessEntity();
    }
}
