# Backend Local Runtime

This backend can be run for real-flow testing with a repo-local Maven wrapper and a MariaDB Docker container whose data stays under `backend/.local/`.

## Prerequisites

- Java 21 on your host
- Docker with `docker compose`
- `curl` or `wget` available for the first `./mvnw` run

## Files

- `mvnw`, `mvnw.cmd`: checked-in Maven wrapper scripts
- `.mvn/wrapper/maven-wrapper.properties`: Maven distribution source
- `compose.yml`: local MariaDB service
- `.env.example`: shared local env template for Compose and Spring Boot

## One-time setup

From `backend/`:

```bash
cp .env.example .env
mkdir -p .local/mariadb-data
chmod +x mvnw
```

## Root-level workflow

From the repo root, the convenience scripts can manage the full local stack:

```bash
./scripts/dev-up.sh
./scripts/smoke-test.sh
./scripts/verify-builds.sh
./scripts/dev-down.sh
```

`scripts/dev-up.sh` bootstraps missing local env files and backend runtime directories before starting MariaDB, the backend, and the frontend with prefixed logs. The script starts the backend with local demo staff seeding enabled by default; set `DEMO_SEED_DATA=false` in `backend/.env` when you need a startup with no demo staff side effects.

## Start MariaDB

From `backend/`:

```bash
docker compose up -d
docker compose logs -f mariadb
```

MariaDB listens on `localhost:3306` and persists data in `backend/.local/mariadb-data`.

## Start the backend

From `backend/`:

```bash
set -a
source .env
set +a
./mvnw -Dmaven.repo.local=.m2/repository spring-boot:run
```

On first run, `./mvnw` downloads Maven into the wrapper cache. Spring Boot then connects to MariaDB and Flyway applies the existing schema and seed migrations.

## Local demo staff seed data

When `DEMO_SEED_DATA=true`, backend startup seeds prototype-only restaurant staff records after Flyway has created the schema:

- three active cashiers: `demo-cashier-1` through `demo-cashier-3`
- three active cooks: `demo-cook-1` through `demo-cook-3`
- three active drivers: `demo-driver-1` through `demo-driver-3`, each with vehicle display details

The seed is idempotent: repeated local starts update the same stable IDs instead of inserting duplicates. The frontend also preloads those same staff IDs as local demo logins using the shared password `demo`, so the cashier, cook, and driver pages can be demonstrated without manual browser setup. These records are for local demonstrations only and are not production staff management, scheduling, permissions, or payroll data. Manual delivery assignment can reference a seeded driver by `demoDriverId` while still accepting explicitly supplied driver details for prototype flexibility.

## Run tests

From `backend/`:

```bash
./mvnw -Dmaven.repo.local=.m2/repository test
```

Tests use H2 from `src/test/resources/application-test.yml`, so MariaDB is not required for `test`.

## Stop or reset MariaDB

From `backend/`:

```bash
docker compose down
docker compose down -v
rm -rf .local/mariadb-data
```

`down -v` and deleting `.local/mariadb-data` permanently removes only this repo's local MariaDB state.

## End-to-end alignment

- Backend default port: `8080`
- Frontend default backend URL: `http://localhost:8080`
- MariaDB default JDBC URL: `jdbc:mariadb://localhost:3306/restaurant_delivery`

With the backend running on `8080`, the frontend can continue using its existing local defaults.
