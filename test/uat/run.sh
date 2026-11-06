#!/usr/bin/env bash
# Web user-acceptance tests for OpenBoxes: browser flows against the sealed e2e stack.
#
# usage: test/uat/run.sh [--tag <name>] [--db mysql:8.0.36] [--known-gaps] [--headed] [--keep-up] [--reuse]
#                        [-- <extra playwright args>]
#        test/uat/run.sh --unit       the harness's own unit tests (node --test test/uat/test)
#
#   --tag         only the tests tagged @<name> (a flow name, or `smoke`); default: everything
#   --db          database image (default mariadb:11.4; mysql:8.0.36 is the other supported one)
#   --known-gaps  run the known-gap tests as normal tests (a fixed gap then passes)
#   --headed      show the browser (a person can watch)
#   --keep-up     leave the stack running afterwards (E2E_PROJECT=... test/e2e/down.sh stops it)
#   --reuse       use a stack already up on the UAT ports (skip build/up/seed; implies --keep-up)
#
# The UAT stack uses its own compose project and ports so it never touches an e2e stack on the default
# ones: E2E_PROJECT (openboxes-uat-web-<checkout hash>), E2E_HTTP_PORT 28080, E2E_DB_PORT 23306,
# E2E_LABEL_PORT 28081 (override any of them in the environment).
#
# Reports: build/uat/reports/ (html/, junit.xml, summary.txt, cdn-mirror.{json,txt}, app.log,
# test-results/ with every video, trace and screenshot). The exit status is Playwright's.
set -euo pipefail
UAT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
E2E_DIR="$(cd "$UAT_DIR/../e2e" && pwd)"
# the UAT stack runs on the default profile of test/e2e (lib/common.sh), whatever the environment says
# and builds its WAR from this checkout (an E2E_WAR in the environment is not used)
export E2E_PROFILE=default; unset E2E_ISSUANCE_JOB E2E_ALLOCATION_MAX_ATTEMPTS E2E_WAR
export E2E_PROJECT="${E2E_PROJECT:-openboxes-uat-web-$(printf '%s' "$UAT_DIR" | cksum | cut -c1-6)}"
export E2E_HTTP_PORT="${E2E_HTTP_PORT:-28080}" E2E_DB_PORT="${E2E_DB_PORT:-23306}" E2E_LABEL_PORT="${E2E_LABEL_PORT:-28081}"
# shellcheck source=../e2e/lib/common.sh
source "$E2E_DIR/lib/common.sh"
ulog() { echo "[uat] $*" >&2; }

MODE=web; TAG=""; DB_IMAGE="${E2E_DB_IMAGE:-mariadb:11.4}"; GAPS=0; HEADED=0; KEEP=0; REUSE=0; EXTRA=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    --unit) MODE=unit; shift ;;
    --tag) TAG="$2"; shift 2 ;;
    --db) DB_IMAGE="$2"; shift 2 ;;
    --known-gaps) GAPS=1; shift ;;
    --headed) HEADED=1; shift ;;
    --keep-up) KEEP=1; shift ;;
    --reuse) REUSE=1; KEEP=1; shift ;;
    --) shift; EXTRA=("$@"); break ;;
    -h|--help) sed -n '2,20p' "$0"; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done

if [[ $MODE == unit ]]; then
  # plain node:test files: only Node is needed (no npm ci, no browser)
  node_check
  exec node --test "$UAT_DIR"/test/*.test.js
fi
node_env

if [[ -n "$TAG" && ! "$TAG" =~ ^[a-z][a-z0-9-]*$ ]]; then
  echo "--tag must be a flow name or smoke (lowercase letters, digits, dashes; got $TAG)" >&2; exit 2
fi
case "$DB_IMAGE" in mariadb:11.4|mysql:8.0.36) ;; *) ulog "warning: --db $DB_IMAGE is not one of the supported images (mariadb:11.4, mysql:8.0.36)" ;; esac
export E2E_DB_IMAGE="$DB_IMAGE"

ensure_cdn_mirror

REPORTS="$REPO_ROOT/build/uat/reports"
rm -rf "$REPORTS"; mkdir -p "$REPORTS"
start=$(date +%s)

# shellcheck disable=SC2329 # invoked by the EXIT trap
teardown() {
  if [[ $KEEP -eq 0 ]]; then "$E2E_DIR/down.sh" --logs "$REPORTS/app.log" || true
  else
    compose_running logs --no-color app > "$REPORTS/app.log" 2>&1 || true
    ulog "stack left up at $E2E_BASE_URL (E2E_PROJECT=$E2E_PROJECT E2E_HTTP_PORT=$E2E_HTTP_PORT E2E_DB_PORT=$E2E_DB_PORT E2E_LABEL_PORT=$E2E_LABEL_PORT test/e2e/down.sh stops it)"
  fi
}

# 1. the stack (the e2e stack and its seed)
if [[ $REUSE -eq 0 ]]; then
  trap teardown EXIT
  "$E2E_DIR/up.sh"
else
  curl -fsS -o /dev/null "$E2E_BASE_URL/api/status" || { echo "--reuse: no stack answers at $E2E_BASE_URL" >&2; exit 1; }
  trap teardown EXIT
  ulog "reusing the running stack $E2E_PROJECT (OpenBoxes $(cat "$E2E_WORK/running-sha-$E2E_PROJECT" 2>/dev/null || echo '?'))"
fi
ready=$(date +%s)

# 2. the audit checkpoints' database user: SELECT only (test/uat/web/lib/audit-db.ts)
compose_running exec -T db sh -c 'if command -v mariadb >/dev/null; then exec mariadb "$@"; else exec mysql "$@"; fi' sh -uroot -proot <<'SQL'
CREATE USER IF NOT EXISTS 'uat_audit'@'%' IDENTIFIED BY 'uat_audit';
GRANT SELECT ON openboxes.* TO 'uat_audit'@'%';
FLUSH PRIVILEGES;
SQL
ulog "read-only audit user ready (SELECT on openboxes.*)"

# 3. Playwright (test/uat/web shares test/e2e's node_modules: one @playwright/test for both)
[[ -e "$UAT_DIR/web/node_modules" ]] || ln -s ../../e2e/node_modules "$UAT_DIR/web/node_modules"
ARGS=(--project web)
[[ -z "$TAG" ]] || ARGS+=(--grep "@$TAG(\\s|\$)")
[[ $HEADED -eq 0 ]] || ARGS+=(--headed)
status=0
(cd "$UAT_DIR/web" && UAT_REPORTS_DIR="$REPORTS" UAT_KNOWN_GAPS="$GAPS" \
  npx playwright test "${ARGS[@]}" ${EXTRA[@]+"${EXTRA[@]}"}) || status=$?
end=$(date +%s)

{
  echo "OpenBoxes $(cat "$E2E_WORK/running-sha-$E2E_PROJECT" 2>/dev/null || head_sha)  db=$DB_IMAGE  tag=${TAG:-all}  known-gaps=$GAPS  exit=$status"
  echo "stack ready in $(( ready - start )) s, tests $(( end - ready )) s, total $(( end - start )) s (teardown not included)"
  python3 - "$REPORTS/junit.xml" <<'PY' || true
import sys, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
n = failed = skipped = 0
for tc in root.iter('testcase'):
    n += 1
    if tc.find('failure') is not None or tc.find('error') is not None: failed += 1
    elif tc.find('skipped') is not None: skipped += 1
print(f"web: {n} tests, {n - failed - skipped} passed, {failed} failed, {skipped} skipped (known gaps)")
PY
  [[ -f "$REPORTS/cdn-mirror.txt" ]] && cat "$REPORTS/cdn-mirror.txt"
} | tee "$REPORTS/summary.txt" >&2
ulog "reports: $REPORTS (html/index.html, summary.txt)"
exit $status
