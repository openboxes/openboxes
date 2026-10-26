#!/usr/bin/env bash
# Bring up OpenBoxes (this checkout's WAR) for the end-to-end suite, then seed it.
#
# usage: test/e2e/up.sh [--no-seed]
#
#   1. test/e2e/build-war.sh (skipped when build/e2e/war/openboxes-<HEAD sha>.war exists)
#   2. docker compose (test/e2e/docker/compose.yml): mariadb:11.4 (tmpfs, fresh every up), the WAR on
#      eclipse-temurin:8-jre-jammy (grails.env=prod, test/e2e/docker/openboxes.yml), the label stub,
#      and a socat gateway; everything but the gateway is on an internal network with no route out.
#      Host ports (127.0.0.1 only): HTTP $E2E_HTTP_PORT (18080), MariaDB $E2E_DB_PORT (13306),
#      label stub $E2E_LABEL_PORT (18081).
#   3. waits for /openboxes/api/status, then test/e2e/seed.sh (fixtures/seed.sql + the API seed).
#
# Environment: E2E_HTTP_PORT, E2E_DB_PORT, E2E_LABEL_PORT, E2E_APP_XMX (1536m), E2E_APP_MEM (2g),
# E2E_DB_IMAGE (mariadb:11.4; mysql:8.0.36 also works), E2E_PROJECT (compose project name),
# E2E_BOOT_TIMEOUT (900 s).
set -euo pipefail
E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "$E2E_DIR/lib/common.sh"

SEED=1
while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-seed) SEED=0; shift ;;
    -h|--help) sed -n '2,17p' "$0"; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done

MY_CNF="$(my_cnf)"
WAR="$("$E2E_DIR/build-war.sh")"
FULL="$(basename "$WAR" .war)"; FULL="${FULL#openboxes-}"
mkdir -p "$E2E_WORK"
export E2E_LABEL_PORT="${E2E_LABEL_PORT:-18081}"
sed -e "s/@E2E_HTTP_PORT@/$E2E_HTTP_PORT/g" -e "s/@E2E_LABEL_PORT@/$E2E_LABEL_PORT/g" \
  "$E2E_DIR/docker/openboxes.yml" > "$E2E_WORK/openboxes-$E2E_HTTP_PORT.yml"
export E2E_WAR="$WAR" E2E_APP_CONFIG="$E2E_WORK/openboxes-$E2E_HTTP_PORT.yml" E2E_MY_CNF="$MY_CNF"
export E2E_PROJECT E2E_HTTP_PORT E2E_DB_PORT

start=$(date +%s)
log "starting $E2E_PROJECT (OpenBoxes $FULL) on 127.0.0.1:$E2E_HTTP_PORT"
compose up -d --force-recreate --renew-anon-volumes >&2
echo "$FULL" > "$E2E_WORK/running-sha-$E2E_PROJECT"

log "waiting for OpenBoxes (Liquibase runs ~1000 changesets on the empty database)"
deadline=$(( $(date +%s) + ${E2E_BOOT_TIMEOUT:-900} ))
until curl -fsS -o /dev/null "$E2E_BASE_URL/api/status" 2>/dev/null; do
  if [[ $(date +%s) -gt $deadline ]]; then
    compose logs --tail 80 app >&2 || true
    echo "OpenBoxes did not come up in time" >&2; exit 1
  fi
  if [[ -z "$(compose ps -q --status running app)" ]]; then
    compose logs --tail 80 app >&2 || true
    echo "the app container stopped" >&2; exit 1
  fi
  sleep 5
done
log "OpenBoxes up in $(( $(date +%s) - start )) s at $E2E_BASE_URL"

if [[ $SEED -eq 1 ]]; then
  "$E2E_DIR/seed.sh"
fi
