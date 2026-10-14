#!/usr/bin/env bash
# Stop and remove the e2e stack (containers, networks, the tmpfs database). usage: test/e2e/down.sh [--logs <file>]
set -euo pipefail
E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "$E2E_DIR/lib/common.sh"
LOGS=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --logs) LOGS="$2"; shift 2 ;;
    -h|--help) sed -n '2p' "$0"; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done
if [[ -n "$LOGS" ]]; then
  mkdir -p "$(dirname "$LOGS")"
  compose_running logs --no-color app > "$LOGS" 2>&1 || true
fi
compose_running down --volumes --remove-orphans >&2
rm -f "$E2E_WORK/running-sha-$E2E_PROJECT"
log "stopped $E2E_PROJECT"
