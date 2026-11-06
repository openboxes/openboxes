#!/usr/bin/env bash
# Bring up OpenBoxes (this checkout's WAR) for the end-to-end suite, then seed it.
#
# usage: test/e2e/up.sh [--no-seed] [--profile default|scenarios] [--issuance-job on|off]
#                       [--allocation-max-attempts <n>] [--war <path>]
#
#   1. test/e2e/build-war.sh (skipped when build/e2e/war/openboxes-<HEAD sha>.war exists), or the
#      prebuilt WAR of --war <path> / E2E_WAR, used as is
#   2. docker compose (test/e2e/docker/compose.yml): mariadb:11.4 (tmpfs, fresh every up), the WAR on
#      eclipse-temurin:8-jre-jammy (grails.env=prod, test/e2e/docker/openboxes.yml rendered for the
#      profile), the label stub, and a socat gateway; the `scenarios` profile adds the webhook sink and
#      its gateway. Everything but the gateways is on an internal network with no route out.
#      Host ports (127.0.0.1 only): HTTP $E2E_HTTP_PORT (18080), MariaDB $E2E_DB_PORT (13306),
#      label stub $E2E_LABEL_PORT (18081), webhook sink $E2E_SINK_PORT (scenarios only); the
#      `scenarios` profile uses 38080, 33306, 38081, 38082 (48080... with an attempt limit other than 3).
#   3. waits for /openboxes/api/status, then test/e2e/seed.sh (fixtures/seed.sql + the API seed).
#
# --profile, --issuance-job, --allocation-max-attempts: see lib/common.sh (E2E_PROFILE,
# E2E_ISSUANCE_JOB, E2E_ALLOCATION_MAX_ATTEMPTS).
# Environment: E2E_HTTP_PORT, E2E_DB_PORT, E2E_LABEL_PORT, E2E_SINK_PORT, E2E_APP_XMX (1536m),
# E2E_APP_MEM (2g), E2E_DB_IMAGE (mariadb:11.4; mysql:8.0.36 also works), E2E_PROJECT (compose
# project name), E2E_BOOT_TIMEOUT (900 s), E2E_WAR.
set -euo pipefail
E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

SEED=1
while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-seed) SEED=0; shift ;;
    --profile) export E2E_PROFILE="$2"; shift 2 ;;
    --issuance-job) export E2E_ISSUANCE_JOB="$2"; shift 2 ;;
    --allocation-max-attempts) export E2E_ALLOCATION_MAX_ATTEMPTS="$2"; shift 2 ;;
    --war) export E2E_WAR="$2"; shift 2 ;;
    -h|--help) sed -n '2,22p' "$0"; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done
# after the arguments: the profile decides the project, the ports and the config values
# shellcheck source=lib/common.sh
source "$E2E_DIR/lib/common.sh"

# Render the config template: `#@` comment lines go, an empty value drops its placeholder's line.
render_app_config() { # $1 = template, $2 = output, then NAME=value pairs
  local in="$1" out="$2" args=(-e '/^[[:space:]]*#@/d') pair name value
  shift 2
  for pair in "$@"; do
    name="${pair%%=*}"; value="${pair#*=}"
    if [[ -z "$value" ]]; then args+=(-e "/@$name@/d"); else args+=(-e "s|@$name@|$value|g"); fi
  done
  sed "${args[@]}" "$in" > "$out"
  if grep -q '@E2E_[A-Z_]*@' "$out"; then
    echo "$out still has a placeholder: $(grep -o '@E2E_[A-Z_]*@' "$out" | sort -u | tr '\n' ' ')" >&2
    return 1
  fi
}

MY_CNF="$(my_cnf)"
if [[ -n "${E2E_WAR:-}" ]]; then
  [[ -f "$E2E_WAR" ]] || { echo "--war / E2E_WAR: no file at $E2E_WAR" >&2; exit 2; }
  WAR="$(cd "$(dirname "$E2E_WAR")" && pwd)/$(basename "$E2E_WAR")"
  log "using the prebuilt WAR $WAR (no build)"
else
  WAR="$("$E2E_DIR/build-war.sh")"
fi
FULL="$(basename "$WAR" .war)"; FULL="${FULL#openboxes-}"
mkdir -p "$E2E_WORK"
render_app_config "$E2E_DIR/docker/openboxes.yml" "$E2E_WORK/openboxes-$E2E_HTTP_PORT.yml" \
  E2E_HTTP_PORT="$E2E_HTTP_PORT" E2E_LABEL_PORT="$E2E_LABEL_PORT" \
  E2E_N8N_ENABLED="$CFG_N8N_ENABLED" E2E_N8N_URL="$CFG_N8N_URL" E2E_N8N_HEADER="$CFG_N8N_HEADER" \
  E2E_CRON_POLLING="$CFG_CRON_POLLING" E2E_ISSUANCE_JOB="$CFG_ISSUANCE_JOB" \
  E2E_ALLOCATION_MAX_ATTEMPTS="$CFG_ALLOCATION_MAX_ATTEMPTS"
export E2E_WAR="$WAR" E2E_APP_CONFIG="$E2E_WORK/openboxes-$E2E_HTTP_PORT.yml" E2E_MY_CNF="$MY_CNF"

start=$(date +%s)
log "starting $E2E_PROJECT (profile $E2E_PROFILE, OpenBoxes $FULL) on 127.0.0.1:$E2E_HTTP_PORT"
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
