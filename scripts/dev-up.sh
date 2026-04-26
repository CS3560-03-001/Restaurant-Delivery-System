#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/dev-lib.sh"

cleanup() {
  set +e
  if [ -n "${FRONTEND_PID:-}" ]; then
    stop_pid "$FRONTEND_PID" "frontend"
  fi
  if [ -n "${BACKEND_PID:-}" ]; then
    stop_pid "$BACKEND_PID" "backend"
  fi
  if [ -d "$BACKEND_DIR" ]; then
    docker compose -f "$BACKEND_COMPOSE_FILE" down >/dev/null 2>&1 || true
  fi
  write_state
}

trap cleanup EXIT

require_command docker
require_command curl
require_command npm
require_command java

bootstrap_env_file "$BACKEND_ENV_FILE" "$BACKEND_ENV_EXAMPLE"
bootstrap_env_file "$FRONTEND_ENV_FILE" "$FRONTEND_ENV_EXAMPLE"
prepare_backend_runtime

set -a
# shellcheck disable=SC1090
source "$BACKEND_ENV_FILE"
set +a

log "startup" "Starting MariaDB"
docker compose -f "$BACKEND_COMPOSE_FILE" up -d mariadb
wait_for_mariadb 180

BACKEND_LOG_FILE="$LOG_DIR/backend.log"
FRONTEND_LOG_FILE="$LOG_DIR/frontend.log"

log "startup" "Starting backend"
start_logged_command "$BACKEND_DIR" "$BACKEND_ENV_FILE" "$BACKEND_LOG_FILE" backend env DEMO_SEED_DATA="${DEMO_SEED_DATA:-true}" SPRING_OUTPUT_ANSI_ENABLED=always ./mvnw -Dmaven.repo.local=.m2/repository spring-boot:run
BACKEND_PID="$STARTED_PID"
write_state
wait_for_http "Backend" "http://127.0.0.1:${SERVER_PORT:-8080}/api/health" 180 health

log "startup" "Starting frontend"
start_logged_command "$FRONTEND_DIR" "$FRONTEND_ENV_FILE" "$FRONTEND_LOG_FILE" frontend env FORCE_COLOR=1 npm run dev -- --host 127.0.0.1
FRONTEND_PID="$STARTED_PID"
write_state
wait_for_http "Frontend" "http://127.0.0.1:5173/" 180

pass "Local stack is running"
log "info" "Press Ctrl-C to stop the stack"

set +e
wait -n "$BACKEND_PID" "$FRONTEND_PID"
service_exit=$?
set -e

log "error" "A managed service exited with status $service_exit; stopping the stack"
exit "$service_exit"
