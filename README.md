# Restaurant Delivery System

This repository contains a local demo of a restaurant delivery workflow built as a SvelteKit frontend, a Spring Boot backend, and a MariaDB database.

The main demo flow is:

```text
account -> order -> payment -> status
```

## Tech Stack

- SvelteKit frontend in `frontend/`
- Spring Boot backend in `backend/`
- MariaDB runtime via Docker Compose in `backend/compose.yml`

The frontend keeps the browser-facing API at `/api/*` and proxies those requests to the backend.

## Prerequisites

Install these on your machine before running the demo:

- Node.js with `npm`
- Java 21
- Docker with `docker compose`
- `curl`
- `wget` for the first Maven wrapper bootstrap if your environment needs it

## Quick Start

From the repository root:

```bash
./scripts/dev-up.sh
```

This script:

- creates `backend/.env` and `frontend/.env` from checked-in examples if they do not exist
- creates backend local runtime directories under `backend/.local/`
- starts MariaDB first
- waits for the backend health endpoint to come up
- starts the frontend dev server
- streams prefixed logs for all managed services in one terminal

After the stack starts, run the smoke test from another terminal:

```bash
./scripts/smoke-test.sh
```

When you are done:

```bash
./scripts/dev-down.sh
```

## Local URLs And Ports

- Frontend: `http://localhost:5173`
- Backend base URL: `http://localhost:8080`
- Backend health: `http://localhost:8080/api/health`
- MariaDB: `localhost:3306`

## Browser Demo Walkthrough

1. Open `http://localhost:5173`.
2. The app redirects to `/account`.
3. Enter customer details and save the customer profile.
4. Build one or more pizzas on `/order`.
5. Continue to checkout and review the backend-calculated total on `/payment`.
6. Submit the mock payment.
7. Continue to `/status` and watch delivery status polling refresh every 5 seconds.

The demo is intentionally form-driven and shows JSON-backed workflow steps from customer creation through order tracking.

## Environment Files

The full-stack launcher bootstraps these files automatically if they are missing:

- `backend/.env` from `backend/.env.example`
- `frontend/.env` from `frontend/.env.example`

### Backend Variables

`backend/.env.example` defines:

- `SERVER_PORT=8080`
- `DB_URL=jdbc:mariadb://localhost:${MARIADB_PORT:-3306}/restaurant_delivery`
- `DB_USERNAME=restaurant`
- `DB_PASSWORD=restaurant`
- `FRONTEND_ORIGIN=http://localhost:5173`
- `MARIADB_DATABASE=restaurant_delivery`
- `MARIADB_ROOT_PASSWORD=restaurant-root`

### Frontend Variables

`frontend/.env.example` defines:

- `BACKEND_API_BASE_URL=http://localhost:8080`

The frontend server routes use this value to forward `/api/*` requests to Spring Boot.

## Verify Builds

To verify the frontend and backend without starting the live stack:

```bash
./scripts/verify-builds.sh
```

This runs:

- `npm run check` in `frontend/`
- `npm run build` in `frontend/`
- `./mvnw ... test` in `backend/`
- `./mvnw ... package -DskipTests` in `backend/`

## Manual Startup

Use this path when you want to debug one layer at a time instead of using the orchestrated scripts.

### Start MariaDB

From `backend/`:

```bash
cp .env.example .env
mkdir -p .local/mariadb-data
chmod +x mvnw
docker compose up -d
```

### Start The Backend

From `backend/`:

```bash
set -a
source .env
set +a
./mvnw -Dmaven.repo.local=.m2/repository spring-boot:run
```

### Start The Frontend

From `frontend/`:

```bash
npm install
npm run dev -- --host 127.0.0.1
```

If `frontend/.env` is missing, create it from the example first:

```bash
cp .env.example .env
```

## Frontend Production Preview

If you want to preview the built frontend instead of the dev server:

```bash
cd frontend
npm run build
npm run preview
```

This is optional and is not part of the main full-stack quick start. It still requires a reachable backend if you want the full workflow to succeed.

## Troubleshooting

- If `./scripts/dev-up.sh` fails immediately, confirm `docker`, `curl`, `npm`, and `java` are installed and available on your `PATH`.
- If startup hangs, check for port conflicts on `5173`, `8080`, or `3306`.
- On first backend startup, the Maven wrapper may download dependencies before Spring Boot begins.
- If a previous run ended unexpectedly, use `./scripts/dev-down.sh` before trying again.
- If the frontend loads but API actions fail, confirm `frontend/.env` points `BACKEND_API_BASE_URL` at the running backend.

## Repo Layout

- `frontend/`: SvelteKit app, frontend docs, and browser-facing `/api/*` proxy routes
- `backend/`: Spring Boot application, Docker Compose file, Flyway migrations, and backend runtime docs
- `scripts/`: root-level orchestration, smoke test, and build verification scripts

## More Docs

- `backend/README.md` for backend runtime details
- `frontend/README.md` for frontend-only development details
