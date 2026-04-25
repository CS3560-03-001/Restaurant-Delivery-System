#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/dev-lib.sh"

require_command docker

load_state

stop_pid "${FRONTEND_TAIL_PID:-}" "frontend log tail"
stop_pid "${BACKEND_TAIL_PID:-}" "backend log tail"
stop_pid "${FRONTEND_PID:-}" "frontend"
stop_pid "${BACKEND_PID:-}" "backend"

if docker compose -f "$BACKEND_COMPOSE_FILE" down >/dev/null 2>&1; then
  pass "MariaDB stack stopped"
else
  fail "Failed to stop the MariaDB Compose stack"
fi
