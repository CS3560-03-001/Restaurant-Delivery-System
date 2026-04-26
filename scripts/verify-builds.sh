#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/dev-lib.sh"

require_command npm
require_command java

log "verify" "Running frontend check"
(cd "$FRONTEND_DIR" && npm run check)

log "verify" "Running frontend build"
(cd "$FRONTEND_DIR" && npm run build)

log "verify" "Running backend tests"
(cd "$BACKEND_DIR" && ./mvnw -Dmaven.repo.local=.m2/repository test)

log "verify" "Running backend package"
(cd "$BACKEND_DIR" && ./mvnw -Dmaven.repo.local=.m2/repository package -DskipTests)

pass "Build verification passed"
