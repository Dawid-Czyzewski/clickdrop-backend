package pl.clickdrop.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.services")
public record GatewayProperties(
        String auth,
        String economy,
        String inventory
) {}
