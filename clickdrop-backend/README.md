# api-gateway

Single HTTP entry for ClickDrop microservices (port **8080**).

| Path | Upstream |
|------|----------|
| `/api/auth/**`, `/api/users/**` | auth-service `:8081` |
| `/api/economy/inventory/**`, `/api/economy/trade-up` | inventory-service `:8083` |
| `/api/economy/**` | economy-service `:8082` |

See `../ARCHITECTURE.md`.
