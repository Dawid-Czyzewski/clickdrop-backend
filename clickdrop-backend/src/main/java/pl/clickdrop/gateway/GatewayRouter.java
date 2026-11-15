package pl.clickdrop.gateway;

import org.springframework.stereotype.Component;

@Component
public class GatewayRouter {

    private final GatewayProperties services;

    public GatewayRouter(GatewayProperties services) {
        this.services = services;
    }

    public String resolveUpstream(String path) {
        if (path.startsWith("/api/auth/") || path.equals("/api/auth")
                || path.startsWith("/api/users/") || path.equals("/api/users")
                || path.startsWith("/api/internal/")) {
            return services.auth();
        }
        if (path.startsWith("/api/economy/inventory")
                || path.equals("/api/economy/trade-up")) {
            return services.inventory();
        }
        if (path.startsWith("/api/economy/") || path.equals("/api/economy")) {
            return services.economy();
        }
        throw new UnknownRouteException(path);
    }
}
