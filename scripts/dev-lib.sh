#!/usr/bin/env bash

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$REPO_ROOT/backend"
FRONTEND_DIR="$REPO_ROOT/frontend"
BACKEND_COMPOSE_FILE="$BACKEND_DIR/compose.yml"
BACKEND_ENV_FILE="$BACKEND_DIR/.env"
BACKEND_ENV_EXAMPLE="$BACKEND_DIR/.env.example"
FRONTEND_ENV_FILE="$FRONTEND_DIR/.env"
FRONTEND_ENV_EXAMPLE="$FRONTEND_DIR/.env.example"
STATE_DIR="$BACKEND_DIR/.local/dev-orchestration"
LOG_DIR="$STATE_DIR/logs"
STATE_FILE="$STATE_DIR/state.env"

color_enabled() {
  [ -t 1 ] && [ -z "${NO_COLOR:-}" ]
}

label_color() {
  case "$1" in
    info) printf '36' ;;
    frontend) printf '92' ;;
    backend) printf '35' ;;
    health) printf '33' ;;
    startup) printf '34' ;;
    ready) printf '32' ;;
    cleanup) printf '90' ;;
    bootstrap) printf '94' ;;
    verify) printf '34' ;;
    smoke) printf '36' ;;
    error|FAIL) printf '31' ;;
    PASS) printf '32' ;;
    *) printf '37' ;;
  esac
}

print_label() {
  local label="$1"
  local color_code

  if color_enabled; then
    color_code="$(label_color "$label")"
    printf '\033[%sm[%s]\033[0m' "$color_code" "$label"
  else
    printf '[%s]' "$label"
  fi
}

print_line() {
  local label="$1"
  local message="$2"
  local color_code

  if color_enabled; then
    color_code="$(label_color "$label")"
    printf '\033[%sm[%s] %s\033[0m\n' "$color_code" "$label" "$message"
  else
    printf '[%s] %s\n' "$label" "$message"
  fi
}

print_prefixed_line() {
  local label="$1"
  local message="$2"
  local color_code

  if color_enabled; then
    color_code="$(label_color "$label")"
    printf '\033[%sm[%s]\033[0m %s\n' "$color_code" "$label" "$message"
  else
    printf '[%s] %s\n' "$label" "$message"
  fi
}

log() {
  print_prefixed_line "$1" "$2"
}

pass() {
  print_line "PASS" "$1"
}

fail() {
  if color_enabled; then
    local color_code
    color_code="$(label_color "FAIL")"
    printf '\033[%sm[FAIL] %s\033[0m\n' "$color_code" "$1" >&2
  else
    printf '[FAIL] %s\n' "$1" >&2
  fi
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Missing required command: $1"
}

bootstrap_env_file() {
  local target="$1"
  local example="$2"

  if [ -f "$target" ]; then
    return 0
  fi

  [ -f "$example" ] || fail "Missing template file: $example"
  cp "$example" "$target"
  log "bootstrap" "Created $(basename "$target") from $(basename "$example")"
}

prepare_backend_runtime() {
  mkdir -p "$STATE_DIR" "$LOG_DIR" "$BACKEND_DIR/.local/mariadb-data"
  chmod +x "$BACKEND_DIR/mvnw"
}

write_state() {
  cat >"$STATE_FILE" <<EOF
BACKEND_PID=${BACKEND_PID:-}
FRONTEND_PID=${FRONTEND_PID:-}
BACKEND_TAIL_PID=${BACKEND_TAIL_PID:-}
FRONTEND_TAIL_PID=${FRONTEND_TAIL_PID:-}
EOF
}

load_state() {
  if [ -f "$STATE_FILE" ]; then
    # shellcheck disable=SC1090
    source "$STATE_FILE"
  fi
}

pid_alive() {
  local pid="${1:-}"
  [ -n "$pid" ] && kill -0 "$pid" >/dev/null 2>&1
}

stop_pid() {
  local pid="${1:-}"
  local label="$2"

  if ! pid_alive "$pid"; then
    log "cleanup" "$label already stopped"
    return 0
  fi

  kill "$pid" >/dev/null 2>&1 || true
  for _ in 1 2 3 4 5 6 7 8 9 10; do
    if ! pid_alive "$pid"; then
      log "cleanup" "Stopped $label"
      return 0
    fi
    sleep 1
  done

  kill -9 "$pid" >/dev/null 2>&1 || true
  log "cleanup" "Force-stopped $label"
}

start_logged_command() {
  local workdir="$1"
  local envfile="$2"
  local logfile="$3"
  local prefix="$4"
  local use_color="0"
  shift 4

  if color_enabled; then
    use_color="1"
  fi

  : >"$logfile"

  bash -c '
    set -euo pipefail
    workdir="$1"
    envfile="$2"
    logfile="$3"
    prefix="$4"
    use_color="$5"
    shift 5
    color_enabled() {
      [ "$use_color" = "1" ] && [ -z "${NO_COLOR:-}" ]
    }
    label_color() {
      case "$1" in
        frontend) printf "92" ;;
        backend) printf "35" ;;
        *) printf "37" ;;
      esac
    }
    print_label() {
      local label="$1"
      local color_code

      if color_enabled; then
        color_code="$(label_color "$label")"
        printf "\033[%sm[%s]\033[0m" "$color_code" "$label"
      else
        printf "[%s]" "$label"
      fi
    }
    print_prefixed_line() {
      local label="$1"
      local message="$2"
      local color_code

      if color_enabled; then
        color_code="$(label_color "$label")"
        printf "\033[%sm[%s]\033[0m %s\n" "$color_code" "$label" "$message"
      else
        printf "[%s] %s\n" "$label" "$message"
      fi
    }
    cd "$workdir"
    if [ -n "$envfile" ] && [ -f "$envfile" ]; then
      set -a
      # shellcheck disable=SC1090
      source "$envfile"
      set +a
    fi
    if command -v stdbuf >/dev/null 2>&1; then
      stdbuf -oL -eL "$@"
    else
      "$@"
    fi 2>&1 | while IFS= read -r line; do
      printf "%s\n" "$line" >>"$logfile"
      print_prefixed_line "$prefix" "$line"
    done
  ' _ "$workdir" "$envfile" "$logfile" "$prefix" "$use_color" "$@" &
  STARTED_PID="$!"
}

wait_for_http() {
  local label="$1"
  local url="$2"
  local timeout_seconds="${3:-120}"
  local log_label="${4:-ready}"
  local start_seconds now_seconds http_code

  start_seconds="$(date +%s)"
  while true; do
    http_code="$(curl -sS -o /dev/null -w '%{http_code}' "$url" || true)"
    if [ -n "$http_code" ] && [ "$http_code" != "000" ] && [ "$http_code" -ge 200 ] && [ "$http_code" -lt 400 ]; then
      log "$log_label" "$label responded with HTTP $http_code"
      return 0
    fi

    now_seconds="$(date +%s)"
    if [ $((now_seconds - start_seconds)) -ge "$timeout_seconds" ]; then
      fail "Timed out waiting for $label at $url"
    fi

    sleep 2
  done
}

wait_for_mariadb() {
  local timeout_seconds="${1:-120}"
  local start_seconds now_seconds

  start_seconds="$(date +%s)"
  while true; do
    if docker compose -f "$BACKEND_COMPOSE_FILE" exec -T mariadb mariadb-admin ping -h 127.0.0.1 -u root -p"${MARIADB_ROOT_PASSWORD:-restaurant-root}" --silent >/dev/null 2>&1; then
      log "ready" "MariaDB is accepting connections"
      return 0
    fi

    now_seconds="$(date +%s)"
    if [ $((now_seconds - start_seconds)) -ge "$timeout_seconds" ]; then
      fail "Timed out waiting for MariaDB to become ready"
    fi

    sleep 2
  done
}
