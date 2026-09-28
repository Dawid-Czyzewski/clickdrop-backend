package pl.clickdrop.economy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AuthClientConfig {

    @Bean
    RestClient authRestClient(
            @Value("${app.auth.base-url}") String baseUri,
            @Value("${app.auth.internal-api-key}") String internalApiKey
    ) {
        return RestClient.builder()
                .baseUrl(baseUri)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }
}