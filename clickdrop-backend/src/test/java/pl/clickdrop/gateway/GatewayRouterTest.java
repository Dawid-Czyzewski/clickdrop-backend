package pl.clickdrop.gateway;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayRouterTest {

    private final GatewayRouter router = new GatewayRouter(
            new GatewayProperties(
                    "http://localhost:8081",
                    "http://localhost:8082",
                    "http://localhost:8083"
            )
    );

    @Test
    void routesAuthEconomyAndInventory() {
        assertThat(router.resolveUpstream("/api/auth/login")).isEqualTo("http://localhost:8081");
        assertThat(router.resolveUpstream("/api/economy/balance")).isEqualTo("http://localhost:8082");
        assertThat(router.resolveUpstream("/api/economy/inventory/me")).isEqualTo("http://localhost:8083");
        assertThat(router.resolveUpstream("/api/economy/trade-up")).isEqualTo("http://localhost:8083");
    }

    @Test
    void unknownPathThrows() {
        assertThatThrownBy(() -> router.resolveUpstream("/api/unknown"))
                .isInstanceOf(UnknownRouteException.class);
    }
}