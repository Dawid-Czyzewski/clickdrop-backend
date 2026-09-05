package pl.clickdrop.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class EconomyClientConfig {

    @Bean
    RestClient economyRestClient(@Value("${app.economy.base-url}") String baseUri) {
        return RestClient.builder()
                .baseUrl(baseUri)
                .build();
    }
}