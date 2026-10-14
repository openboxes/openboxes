#!/usr/bin/env bash
# Build, bring up, seed, test and tear down the OpenBoxes end-to-end stack for this checkout.
#
# usage: test/e2e/run.sh [--project mobile-api|flows|all] [--keep-up] [--reuse] [--known-gaps]
#                        [-- <extra playwright args>]
#
#   --project    mobile-api (the mobile API contract), flows (warehouse flows), all (default)
#   --keep-up    leave the stack running afterwards (test/e2e/down.sh stops it)
#   --reuse      use a stack that is already up (skip build/up/seed; implies --keep-up)
#   --known-gaps run every KNOWN-GAP (test.fixme) as a normal test: an open gap fails, a fixed one passes
#
# The WAR is built from this checkout once per HEAD commit (test/e2e/build-war.sh). The CDN mirror
# (test/e2e/cdn-mirror/cache) is fetched first when it is missing.
#
# Reports (build/e2e/reports): junit.xml, html/ (traces of failures inside), contract-coverage.json,
# app.log (the app container log), cdn-mirror.{json,txt} (per page: third-party URLs served from the
# CDN mirror or missing from it), summary.txt. The exit status is Playwright's.
set -euo pipefail
E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "$E2E_DIR/lib/common.sh"

PROJECT=all; KEEP=0; REUSE=0; GAPS=0; EXTRA=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    --project) PROJECT="$2"; shift 2 ;;
    --keep-up) KEEP=1; shift ;;
    --reuse) REUSE=1; KEEP=1; shift ;;
    --known-gaps) GAPS=1; shift ;;
    --) shift; EXTRA=("$@"); break ;;
    -h|--help) sed -n '2,18p' "$0"; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done
case "$PROJECT" in mobile-api|flows|all) ;; *) echo "--project must be mobile-api|flows|all" >&2; exit 2 ;; esac

ensure_cdn_mirror

REPORTS="$E2E_WORK/reports"
rm -rf "$REPORTS"; mkdir -p "$REPORTS"
start=$(date +%s)

# shellcheck disable=SC2329 # invoked by the EXIT trap
teardown() {
  if [[ $KEEP -eq 0 ]]; then "$E2E_DIR/down.sh" --logs "$REPORTS/app.log" || true
  else
    compose_running logs --no-color app > "$REPORTS/app.log" 2>&1 || true
    log "stack left up at $E2E_BASE_URL (test/e2e/down.sh stops it)"
  fi
}

if [[ $REUSE -eq 0 ]]; then
  trap teardown EXIT
  "$E2E_DIR/up.sh"
else
  curl -fsS -o /dev/null "$E2E_BASE_URL/api/status" || { echo "--reuse: no stack answers at $E2E_BASE_URL" >&2; exit 1; }
  trap teardown EXIT
  log "reusing the running stack (OpenBoxes $(cat "$E2E_WORK/running-sha-$E2E_PROJECT" 2>/dev/null || echo '?'))"
fi
ready=$(date +%s)

node_env
PROJECTS=()
if [[ $PROJECT == all ]]; then PROJECTS=(--project mobile-api --project flows); else PROJECTS=(--project "$PROJECT"); fi
[[ $GAPS -eq 0 ]] || export E2E_KNOWN_GAPS=1
status=0
(cd "$E2E_DIR" && E2E_REPORTS_DIR="$REPORTS" npx playwright test "${PROJECTS[@]}" ${EXTRA[@]+"${EXTRA[@]}"}) || status=$?
end=$(date +%s)

{
  echo "OpenBoxes $(cat "$E2E_WORK/running-sha-$E2E_PROJECT" 2>/dev/null || head_sha)  project=$PROJECT  known-gaps=$GAPS  exit=$status"
  echo "stack ready in $(( ready - start )) s, tests $(( end - ready )) s, total $(( end - start )) s (teardown not included)"
  python3 - "$REPORTS/junit.xml" <<'PY' || true
import sys, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
per = {}
for ts in root.iter('testsuite'):
    name = ts.get('name', '')
    suite = 'mobile-api' if name.startswith('mobile-api') else ('flows' if name.startswith('flows') else 'setup')
    for tc in ts.iter('testcase'):
        r = per.setdefault(suite, {'tests': 0, 'failed': 0, 'skipped': 0})
        r['tests'] += 1
        if tc.find('failure') is not None or tc.find('error') is not None: r['failed'] += 1
        elif tc.find('skipped') is not None: r['skipped'] += 1
for k, v in sorted(per.items()):
    print(f"{k}: {v['tests']} tests, {v['tests'] - v['failed'] - v['skipped']} passed, {v['failed']} failed, {v['skipped']} skipped (KNOWN-GAP fixme)")
PY
  [[ -f "$REPORTS/cdn-mirror.txt" ]] && cat "$REPORTS/cdn-mirror.txt"
  [[ -f "$REPORTS/contract-coverage.json" ]] && python3 -c "import json,sys;d=json.load(open(sys.argv[1]));print(f\"contract: {d['entries']} entries, {d['percentExercised']} % exercised, {d['percentPassing']} % with a passing test, {d['percentConformant']} % conformant\")" "$REPORTS/contract-coverage.json" || true
} | tee "$REPORTS/summary.txt" >&2
exit $status
