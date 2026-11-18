# shellcheck shell=bash
# Shared settings for the test/e2e scripts (build-war.sh, up.sh, down.sh, seed.sh, run.sh) and
# test/uat/run.sh. The sourcing script sets E2E_DIR (test/e2e) before sourcing this file.
# shellcheck disable=SC2034 # the variables below are used by the sourcing scripts

log() { echo "[e2e] $*" >&2; }

# The repository root: the OBAF checkout under test (test/e2e/../..).
REPO_ROOT="$(cd "$E2E_DIR/../.." && pwd)"
# Work files (WAR, generated config, reports) live under build/, which git ignores.
E2E_WORK="$REPO_ROOT/build/e2e"

# The stack profile (up.sh / run.sh --profile, or E2E_PROFILE):
#   default    the integrations off and the polling crons parked (docker/openboxes.yml);
#   scenarios  event delivery to the webhook sink (docker/webhooksink.py), the four polling crons
#              every 20 s, the automatic issuance job off or on (--issuance-job, E2E_ISSUANCE_JOB;
#              default off, the branch default) and the allocation job's attempt limit
#              (--allocation-max-attempts, E2E_ALLOCATION_MAX_ATTEMPTS; default 3, the production value).
# Each profile, and each attempt limit of `scenarios`, has its own compose project, loopback ports and
# reports directory, so such stacks run side by side. CFG_* fill the placeholders of docker/openboxes.yml
# (an empty value drops the line).
E2E_PROFILE="${E2E_PROFILE:-default}"
case "$E2E_PROFILE" in
  default)
    if [[ -n "${E2E_ISSUANCE_JOB:-}${E2E_ALLOCATION_MAX_ATTEMPTS:-}" ]]; then
      echo "--issuance-job and --allocation-max-attempts (E2E_ISSUANCE_JOB, E2E_ALLOCATION_MAX_ATTEMPTS) need --profile scenarios" >&2
      exit 2
    fi
    E2E_STACK_SUFFIX=""; E2E_PORT_PREFIX=1
    CFG_N8N_ENABLED=false; CFG_N8N_URL="http://127.0.0.1:9/"; CFG_N8N_HEADER=""
    CFG_CRON_POLLING="0 0 0 1 1 ? 2099"; CFG_ISSUANCE_JOB=""; CFG_ALLOCATION_MAX_ATTEMPTS=""
    ;;
  scenarios)
    E2E_ISSUANCE_JOB="${E2E_ISSUANCE_JOB:-off}"
    E2E_ALLOCATION_MAX_ATTEMPTS="${E2E_ALLOCATION_MAX_ATTEMPTS:-3}"
    case "$E2E_ISSUANCE_JOB" in
      on) CFG_ISSUANCE_JOB=true ;;
      off) CFG_ISSUANCE_JOB=false ;;
      *) echo "--issuance-job must be on or off (got $E2E_ISSUANCE_JOB)" >&2; exit 2 ;;
    esac
    if [[ ! "$E2E_ALLOCATION_MAX_ATTEMPTS" =~ ^[1-9][0-9]{0,3}$ ]]; then
      echo "--allocation-max-attempts must be a whole number from 1 to 9999 (got $E2E_ALLOCATION_MAX_ATTEMPTS)" >&2
      exit 2
    fi
    # the production limit on 3xxxx ports, any other limit on 4xxxx (set the ports for a third one)
    if [[ $E2E_ALLOCATION_MAX_ATTEMPTS == 3 ]]; then
      E2E_STACK_SUFFIX="-scenarios"; E2E_PORT_PREFIX=3
    else
      E2E_STACK_SUFFIX="-scenarios-a$E2E_ALLOCATION_MAX_ATTEMPTS"; E2E_PORT_PREFIX=4
    fi
    CFG_N8N_ENABLED=true; CFG_N8N_URL="http://webhooksink:8082/webhook/events"; CFG_N8N_HEADER="e2e"
    CFG_CRON_POLLING="0/20 * * * * ?"; CFG_ALLOCATION_MAX_ATTEMPTS="$E2E_ALLOCATION_MAX_ATTEMPTS"
    ;;
  *) echo "--profile must be default or scenarios (got $E2E_PROFILE)" >&2; exit 2 ;;
esac
# One compose project per checkout and profile, so a stack of another checkout or worktree is never touched.
E2E_PROJECT_GIVEN="${E2E_PROJECT:-}"
E2E_PROJECT="${E2E_PROJECT:-openboxes-e2e$E2E_STACK_SUFFIX-$(printf '%s' "$REPO_ROOT" | cksum | cut -c1-6)}"
# Loopback ports (override when busy): 18080, 13306, 18081 (18082) on the default profile.
E2E_HTTP_PORT="${E2E_HTTP_PORT:-${E2E_PORT_PREFIX}8080}"
E2E_DB_PORT="${E2E_DB_PORT:-${E2E_PORT_PREFIX}3306}"
E2E_LABEL_PORT="${E2E_LABEL_PORT:-${E2E_PORT_PREFIX}8081}"
# the webhook sink (scenarios only; nothing listens there on the default profile)
E2E_SINK_PORT="${E2E_SINK_PORT:-${E2E_PORT_PREFIX}8082}"
export E2E_PROFILE E2E_PROJECT E2E_HTTP_PORT E2E_DB_PORT E2E_LABEL_PORT E2E_SINK_PORT
E2E_BASE_URL="http://127.0.0.1:${E2E_HTTP_PORT}/openboxes"
E2E_REPORTS="$E2E_WORK/reports$E2E_STACK_SUFFIX"
# The MariaDB/MySQL server config of the stack (utf8mb3, UTC, sql_mode), shared with the backend suite.
E2E_MY_CNF_FILE="$REPO_ROOT/src/test/resources/regression-testcontainers/my.cnf"

# The commit under test: the checkout's own HEAD.
head_sha() {
  git -C "$REPO_ROOT" rev-parse HEAD
}

# JDK 8 for the Gradle build: OBAF_JAVA_HOME, else JAVA_HOME. Prints the home or fails with a message.
java8_home() {
  local home="${OBAF_JAVA_HOME:-${JAVA_HOME:-}}" version
  if [[ -z "$home" ]]; then
    echo "no JDK 8: set OBAF_JAVA_HOME (or JAVA_HOME) to a JDK 8 home; OpenBoxes builds with Java 8 only" >&2
    return 1
  fi
  if [[ ! -x "$home/bin/java" ]]; then
    echo "no JDK at $home (expected $home/bin/java): set OBAF_JAVA_HOME to a JDK 8 home" >&2
    return 1
  fi
  version="$("$home/bin/java" -version 2>&1 | head -n 1)"
  if [[ "$version" != *'"1.8.'* ]]; then
    echo "$home is not a JDK 8 ($version): set OBAF_JAVA_HOME to a JDK 8 home" >&2
    return 1
  fi
  echo "$home"
}

war_path() { # $1 = full sha
  echo "$E2E_WORK/war/openboxes-$1.war"
}

# The MariaDB/MySQL config file, or a clear error when it is not in the checkout.
my_cnf() {
  if [[ ! -f "$E2E_MY_CNF_FILE" ]]; then
    echo "missing $E2E_MY_CNF_FILE (the database config the stack mounts); see test/e2e/README.md" >&2
    return 1
  fi
  echo "$E2E_MY_CNF_FILE"
}

compose() {
  local profile=()
  [[ $E2E_PROFILE == default ]] || profile=(--profile "$E2E_PROFILE")
  docker compose -p "$E2E_PROJECT" -f "$E2E_DIR/docker/compose.yml" ${profile[@]+"${profile[@]}"} "$@"
}

# compose for commands that do not (re)create containers: the variables the file interpolates only need a value.
compose_running() {
  E2E_WAR=/dev/null E2E_APP_CONFIG=/dev/null E2E_MY_CNF=/dev/null compose "$@"
}

# The command that stops this stack (for messages): down.sh with the settings that select it.
down_hint() {
  local env=""
  [[ $E2E_PROFILE == default ]] || env="E2E_PROFILE=$E2E_PROFILE E2E_ALLOCATION_MAX_ATTEMPTS=$E2E_ALLOCATION_MAX_ATTEMPTS "
  [[ -z "$E2E_PROJECT_GIVEN" ]] || env="${env}E2E_PROJECT='$E2E_PROJECT' "
  echo "${env}test/e2e/down.sh"
}

# --reuse: this project's app and gateway must be running (another stack may answer on the same port).
require_running_stack() {
  local svc
  for svc in app gateway; do
    if [[ -z "$(compose_running ps -q --status running "$svc" 2>/dev/null)" ]]; then
      echo "--reuse: no running stack of $E2E_PROJECT (profile $E2E_PROFILE; $svc is not running)" >&2; return 1
    fi
  done
  curl -fsS -o /dev/null "$E2E_BASE_URL/api/status" || { echo "--reuse: no stack answers at $E2E_BASE_URL" >&2; return 1; }
}

node_major() {
  node -p 'process.versions.node.split(".")[0]' 2>/dev/null || echo 0
}

# Node >= 20 on PATH: NODE_HOME, else what is on PATH, else the newest nvm v22 install.
node_check() {
  local n
  if [[ -n "${NODE_HOME:-}" ]]; then
    PATH="$NODE_HOME/bin:$PATH"
  elif [[ "$(node_major)" -lt 20 ]]; then
    n="$(find "$HOME/.nvm/versions/node" -mindepth 1 -maxdepth 1 -type d -name 'v22*' 2>/dev/null | sort -V | tail -1)"
    if [[ -n "$n" && -x "$n/bin/node" ]]; then PATH="$n/bin:$PATH"; fi
  fi
  export PATH
  if [[ "$(node_major)" -lt 20 ]]; then
    echo "Node >= 20 is required (found: $(node --version 2>/dev/null || echo none)): set NODE_HOME or put Node 20+ on PATH" >&2
    return 1
  fi
}

# node_check, then test/e2e/node_modules (npm ci, once) and Playwright's Chromium (`playwright
# install chromium` on every run: a no-op when the browser of this Playwright version is installed).
node_env() {
  node_check
  if [[ ! -d "$E2E_DIR/node_modules/@playwright/test" ]]; then
    log "npm ci in test/e2e"; (cd "$E2E_DIR" && npm ci --no-audit --no-fund >&2)
  fi
  (cd "$E2E_DIR" && npx playwright install chromium >&2)
}

# The CDN mirror (cdn-mirror/cache) must exist before a browser test runs: fetch it once when it is
# missing (this uses the internet), else warn about URLs the mirror lacks. Both scan HEAD, the commit
# the WAR is built from (cdn-mirror/fetch.sh).
ensure_cdn_mirror() {
  if [[ ! -f "$E2E_DIR/cdn-mirror/cache/manifest.json" ]]; then
    log "no CDN mirror yet: fetching it once (test/e2e/cdn-mirror/fetch.sh; uses the internet, the tests never do)"
    "$E2E_DIR/cdn-mirror/fetch.sh" || {
      echo "could not build the CDN mirror: run test/e2e/cdn-mirror/fetch.sh on a machine with internet access" >&2
      return 1
    }
  else
    "$E2E_DIR/cdn-mirror/fetch.sh" --check || true
  fi
}
