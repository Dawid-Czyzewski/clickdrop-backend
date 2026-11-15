# inventory-service

Port **8083**. Owns inventory HTTP API (list, sell, trade-up, skin-upgrade).

Paths (unchanged for FE):

- `GET /api/economy/inventory/me`
- `POST /api/economy/inventory/sell-batch`
- `POST /api/economy/inventory/{id}/sell`
- `GET|POST /api/economy/inventory/{id}/upgrade*`
- `POST /api/economy/trade-up`

Uses shared DB `clickdrop_economy` (Flyway stays in economy-service). See `../ARCHITECTURE.md`.
