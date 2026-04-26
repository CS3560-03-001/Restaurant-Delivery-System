# Frontend Development

This directory contains the SvelteKit frontend for the restaurant delivery demo.

The frontend owns the customer-facing flow:

```text
/account -> /order -> /payment -> /status
```

It also exposes browser-facing `/api/*` routes that proxy requests to the backend configured by `BACKEND_API_BASE_URL`.

## What The Frontend Does

- collects customer account details
- builds one or more pizzas in a grouped cart flow
- submits mock payment data
- polls for order status updates
- forwards browser requests from SvelteKit `/api/*` routes to the Spring Boot backend

## Prerequisites

- Node.js with `npm`
- a running backend, usually at `http://localhost:8080`

## Install Dependencies

From `frontend/`:

```bash
npm install
```

## Frontend Environment

Create a local env file if one does not already exist:

```bash
cp .env.example .env
```

Default value:

```bash
BACKEND_API_BASE_URL=http://localhost:8080
```

This is read by the SvelteKit server-side proxy routes, not by browser-side code directly.

## Run The Frontend

From `frontend/`:

```bash
npm run dev
```

Default local URL:

- `http://localhost:5173`

For repo-level orchestration, use `../scripts/dev-up.sh` from the project root instead.

## Development Workflow

1. Start the backend separately.
2. Run `npm run dev` in `frontend/`.
3. Open `http://localhost:5173`.
4. The app redirects to `/account`.
5. Save a customer profile.
6. Build one or more pizzas on `/order`.
7. Continue to `/payment` and submit mock payment details.
8. Confirm `/status` refreshes order state automatically.

## Preview The Production Build

The current frontend supports local previewing of the built app:

```bash
npm run build
npm run preview
```

Use preview mode when you want to validate behavior closer to a built deployment than `npm run dev`.

Notes:

- `npm run preview` depends on a successful `npm run build`
- the previewed app still needs a reachable backend for the full workflow

## Available Scripts

- `npm run dev`: start the Vite development server
- `npm run check`: run SvelteKit sync and type checking
- `npm run build`: create a production build
- `npm run preview`: locally serve the production build
- `npm run test`: run Vitest tests

## Frontend Route Map

Pages:

- `/account`
- `/order`
- `/payment`
- `/status`

Proxy API routes:

- `POST /api/customers`
- `POST /api/orders`
- `POST /api/payments`
- `GET /api/orders/[orderId]/status`

## Common Issues

- If form submissions fail, make sure the backend is running and reachable at `BACKEND_API_BASE_URL`.
- If the frontend starts on a different port, check whether `5173` is already in use.
- If `npm run preview` fails, run `npm run build` first.
- If the UI loads but actions fail, verify that the backend exposes the expected `/api/*` endpoints on port `8080` or update `.env`.
