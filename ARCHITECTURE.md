# ClickDrop microservices

| Service | Port | DB | Owns |
|---------|------|----|------|
| api-gateway (clickdrop-backend) | 8080 | — | Routing |
| auth-service | 8081 | clickdrop_auth | Identity, JWT, XP/level, leaderboard |
| economy-service | 8082 | clickdrop_economy | Wallet, clicks, upgrades, cases, battles, daily, collections |
| inventory-service | 8083 | clickdrop_economy (shared for now) | Inventory API, sell, trade-up, skin-upgrade |

## Routing

`	ext
FE (Next /api BFF or gateway :8080)
  /api/auth/**, /api/users/**     -> auth-service :8081
  /api/economy/inventory/**       -> inventory-service :8083
  /api/economy/trade-up           -> inventory-service :8083
  /api/economy/**                 -> economy-service :8082
`

## Boundaries

- auth does not write wallets or inventory
- economy owns ClickDollars and case/battle/daily rolls (still inserts inventory rows on grant)
- inventory owns list/sell/trade-up/upgrade HTTP APIs

## Follow-up

1. Dedicated clickdrop_inventory database
2. Internal grant API on inventory used by economy
3. Sell credits wallet through economy internal credit API