# ClickDrop backend

ClickDrop is split into small Spring Boot 4 services (Java 26, Maven). The browser talks to the Next.js frontend, which proxies `/api` to these services. There is also an API gateway module (`clickdrop-backend`), but production on MyDevil does not run it. The frontend BFF is the public entry point.

## Services

| Service | Default local port | Database | Notes |
| --- | --- | --- | --- |
| auth-service | 8081 | clickdrop_auth | Accounts, JWT, XP, leaderboard, email verification and password reset |
| economy-service | 8082 | clickdrop_economy | Wallet, clicks, cases, battles, daily rewards. Owns Flyway for this database (V1-V19) |
| inventory-service | 8083 | clickdrop_economy (shared) | Inventory, sell, trade-up, skin-upgrade. Flyway is disabled here |
| clickdrop-backend (gateway) | 8080 | none | Optional local router only |

Auth Flyway lives in `auth-service/src/main/resources/db/migration`. Economy Flyway lives in `economy-service/src/main/resources/db/migration`. JPA uses `ddl-auto: validate`.

## Local run

Each service has its own Maven wrapper. You need Java 26, PostgreSQL, and (for auth) an SMTP server.

1. Create databases `clickdrop_auth` and `clickdrop_economy`.
2. Copy the example config (do not commit real passwords):

```bash
cp auth-service/src/main/resources/application.yaml.example auth-service/src/main/resources/application.yaml
cp economy-service/src/main/resources/application.yaml.example economy-service/src/main/resources/application.yaml
cp inventory-service/src/main/resources/application.yaml.example inventory-service/src/main/resources/application.yaml
```

3. Fill in the database URL, user, password, a shared JWT secret (the same value in all three services), and the shared internal API key (auth and economy). For auth, also set SMTP and `app.frontend-base-url`.
4. Start auth, then economy, then inventory:

```bash
export JAVA_HOME=/path/to/jdk-26
cd auth-service && ./mvnw spring-boot:run
cd economy-service && ./mvnw spring-boot:run
cd inventory-service && ./mvnw spring-boot:run
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

## Environment variables

YAML files have no placeholders, but Spring Boot still accepts overrides. Useful variables:

- `SERVER_PORT`
- `SPRING_DATASOURCE_URL` (JDBC URL)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_FLYWAY_ENABLED`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE`
- `APP_MAIL_FROM` (for example `ClickDrop <clickdrop@dawid-developer.pl>`)
- `APP_FRONTEND_BASE_URL` (links in activation and reset emails)
- `APP_JWT_SECRET` (shared by auth, economy, and inventory)
- `APP_INTERNAL_API_KEY` (auth)
- `APP_AUTH_INTERNAL_API_KEY` and `APP_AUTH_BASE_URL` (economy)
- `APP_ECONOMY_BASE_URL` (auth)

Do not commit `application.yaml` files that contain passwords or keys. Auth and economy keep those files untracked; inventory already gitignores its yaml.

## Production on MyDevil

Live site: `https://clickdrop.dawid-developer.pl`

MyDevil prefixes PostgreSQL names with `p1144_` and truncates them to 16 characters, so the requested names became:

- Auth database and user: `p1144_clickdrop_` (from `clickdrop_auth`)
- Economy / inventory database and user: `p1144_cdecon` (short name `cdecon`, because `clickdrop_economy` would collide after truncation)
- Host: `pgsql87.mydevil.net`

Mailbox: `clickdrop@dawid-developer.pl`. SMTP: `mail87.mydevil.net`, port 587, STARTTLS. From header: `ClickDrop <clickdrop@dawid-developer.pl>`.

Reserved TCP ports (internal only, used by the Next.js proxy):

- auth: 24206
- economy: 57679
- inventory: 51234

Server layout (secrets are not in git):

- JARs: `/usr/home/SatoshiDC/clickdrop/jars/`
- Config: `/usr/home/SatoshiDC/clickdrop/config/secrets.env` and `runtime.env` (chmod 600)
- Scripts: `/usr/home/SatoshiDC/clickdrop/bin/start.sh`, `stop.sh`, `watchdog.sh`
- Logs: `/usr/home/SatoshiDC/clickdrop/logs/`
- Frontend: `/usr/home/SatoshiDC/domains/clickdrop.dawid-developer.pl/public_nodejs/`

Start the three JARs with Java 26 (`java26`), a small heap, and a larger metaspace (Spring Boot 4 does not fit in 80m metaspace). Cron runs `start.sh` at reboot and `watchdog.sh` every 10 minutes. Binexec must stay on. The API gateway is not deployed, and there is no `api.clickdrop` subdomain.

Build fat JARs without packaging a yaml that has secrets (`mvn clean package -DskipTests` after moving local yaml files aside). Override all connection settings with environment variables at runtime.

RAM on the MD1 1 GB plan is tight with three JVMs plus Next.js. Do not lower process counts on other sites without asking. If the account is consistently over the memory limit, a higher plan is the safe fix.
