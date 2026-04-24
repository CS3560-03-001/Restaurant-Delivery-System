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
