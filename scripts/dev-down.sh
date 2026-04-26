#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/dev-lib.sh"

require_command docker

load_state

if [ -n "${FRONTEND_PID:-}" ]; then
  stop_pid "$FRONTEND_PID" "frontend"
fi

if [ -n "${BACKEND_PID:-}" ]; then
  stop_pid "$BACKEND_PID" "backend"
fi

if docker compose -f "$BACKEND_COMPOSE_FILE" down >/dev/null 2>&1; then
  pass "MariaDB stack stopped"
else
  fail "Failed to stop the MariaDB Compose stack"
fi
