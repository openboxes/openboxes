#!/usr/bin/env bash
# Build, bring up, seed, test and tear down the OpenBoxes end-to-end stack for this checkout.
#
# usage: test/e2e/run.sh [--project mobile-api|flows|scenarios|all] [--keep-up] [--reuse] [--known-gaps]
#                        [--profile default|scenarios] [--issuance-job on|off]
#                        [--allocation-max-attempts <n>] [--war <path>] [-- <extra playwright args>]
#
#   --project    mobile-api (the mobile API contract), flows (warehouse flows), scenarios (needs
#                --profile scenarios; not there yet), all (default: mobile-api and flows)
#   --keep-up    leave the stack running afterwards (test/e2e/down.sh stops it)
#   --reuse      use a stack that is already up (skip build/up/seed; implies --keep-up)
#   --known-gaps run every KNOWN-GAP (test.fixme) as a normal test: an open gap fails, a fixed one passes
#   --profile, --issuance-job, --allocation-max-attempts
#                the stack profile and its switches (lib/common.sh); each profile and attempt limit
#                has its own compose project, ports and reports directory
#   --war        a prebuilt WAR (also E2E_WAR), used as is: no build
#
# The WAR is built from this checkout once per HEAD commit (test/e2e/build-war.sh). The CDN mirror
# (test/e2e/cdn-mirror/cache) is fetched first when it is missing.
#
# Reports (build/e2e/reports, build/e2e/reports-scenarios[-a<n>] for the scenarios profile): junit.xml,
# html/ (traces of failures inside), contract-coverage.json, app.log (the app container log),
# cdn-mirror.{json,txt} (per page: third-party URLs served from the CDN mirror or missing from it),
# summary.txt. The exit status is Playwright's.
set -euo pipefail
E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PROJECT=all; KEEP=0; REUSE=0; GAPS=0; EXTRA=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    --project) PROJECT="$2"; shift 2 ;;
    --keep-up) KEEP=1; shift ;;
    --reuse) REUSE=1; KEEP=1; shift ;;
    --known-gaps) GAPS=1; shift ;;
    --profile) export E2E_PROFILE="$2"; shift 2 ;;
    --issuance-job) export E2E_ISSUANCE_JOB="$2"; shift 2 ;;
    --allocation-max-attempts) export E2E_ALLOCATION_MAX_ATTEMPTS="$2"; shift 2 ;;
    --war) export E2E_WAR="$2"; shift 2 ;;
    --) shift; EXTRA=("$@"); break ;;
    -h|--help) sed -n '2,24p' "$0"; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done
# after the arguments: the profile decides the project, the ports and the config values
# shellcheck source=lib/common.sh
source "$E2E_DIR/lib/common.sh"
case "$PROJECT" in mobile-api|flows|scenarios|all) ;; *) echo "--project must be mobile-api|flows|scenarios|all" >&2; exit 2 ;; esac
if [[ $PROJECT == scenarios ]]; then
  [[ $E2E_PROFILE == scenarios ]] || { echo "--project scenarios needs --profile scenarios" >&2; exit 2; }
  if [[ ! -d "$E2E_DIR/tests/scenarios" ]]; then log "no scenarios project yet (test/e2e/tests/scenarios): nothing to run"; exit 0; fi
fi

ensure_cdn_mirror

REPORTS="$E2E_REPORTS"
rm -rf "$REPORTS"; mkdir -p "$REPORTS"
start=$(date +%s)

# shellcheck disable=SC2329 # invoked by the EXIT trap
teardown() {
  if [[ $KEEP -eq 0 ]]; then "$E2E_DIR/down.sh" --logs "$REPORTS/app.log" || true
  else
    compose_running logs --no-color app > "$REPORTS/app.log" 2>&1 || true
    log "stack left up at $E2E_BASE_URL ($(down_hint) stops it)"
  fi
}

if [[ $REUSE -eq 0 ]]; then
  trap teardown EXIT
  "$E2E_DIR/up.sh"
else
  require_running_stack
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
  echo "OpenBoxes $(cat "$E2E_WORK/running-sha-$E2E_PROJECT" 2>/dev/null || head_sha)  profile=$E2E_PROFILE${E2E_ISSUANCE_JOB:+ issuance-job=$E2E_ISSUANCE_JOB}${E2E_ALLOCATION_MAX_ATTEMPTS:+ allocation-max-attempts=$E2E_ALLOCATION_MAX_ATTEMPTS}  project=$PROJECT  known-gaps=$GAPS  exit=$status"
  echo "stack ready in $(( ready - start )) s, tests $(( end - ready )) s, total $(( end - start )) s (teardown not included)"
  python3 - "$REPORTS/junit.xml" <<'PY' || true
import sys, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
per = {}
for ts in root.iter('testsuite'):
    name = ts.get('name', '')
    suite = next((p for p in ('mobile-api', 'flows', 'scenarios') if name.startswith(p)), 'setup')
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
