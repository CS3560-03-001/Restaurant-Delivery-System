#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/dev-lib.sh"

require_command curl

frontend_url="${FRONTEND_URL:-http://127.0.0.1:5173}"
backend_url="${BACKEND_URL:-http://127.0.0.1:8080}"

check_http() {
  local label="$1"
  local url="$2"
  local code

  code="$(curl -s -o /dev/null -w '%{http_code}' "$url/" || true)"
  if [ "$code" = "000" ] || [ -z "$code" ]; then
    fail "$label is unreachable at $url"
  fi
  log "smoke" "$label responded with HTTP $code"
}

check_http "Frontend" "$frontend_url"
check_http "Backend" "$backend_url"

tmp_response="$(mktemp)"
trap 'rm -f "$tmp_response"' EXIT

log "smoke" "Posting a JSON customer payload"
customer_code="$(curl -sS -o "$tmp_response" -w '%{http_code}' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Smoke Test","email":"smoke-test@example.com","phone":"555-0100","address":"123 Test Street"}' \
  "$backend_url/api/customers" || true)"

if [ "$customer_code" != "201" ]; then
  fail "Expected POST /api/customers to return HTTP 201, got ${customer_code:-000}"
fi

if ! grep -q '"customerId"' "$tmp_response"; then
  fail "POST /api/customers did not return a customerId field"
fi

pass "Smoke test passed"
