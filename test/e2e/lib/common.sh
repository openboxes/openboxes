# shellcheck shell=bash
# Shared settings for the test/e2e scripts (build-war.sh, up.sh, down.sh, seed.sh, run.sh) and
# test/uat/run.sh. The sourcing script sets E2E_DIR (test/e2e) before sourcing this file.
# shellcheck disable=SC2034 # the variables below are used by the sourcing scripts

log() { echo "[e2e] $*" >&2; }

# The repository root: the OBAF checkout under test (test/e2e/../..).
REPO_ROOT="$(cd "$E2E_DIR/../.." && pwd)"
# Work files (WAR, generated config, reports) live under build/, which git ignores.
E2E_WORK="$REPO_ROOT/build/e2e"
# One compose project per checkout, so a stack of another checkout or worktree is never touched.
E2E_PROJECT="${E2E_PROJECT:-openboxes-e2e-$(printf '%s' "$REPO_ROOT" | cksum | cut -c1-6)}"
# Loopback ports (override when busy).
E2E_HTTP_PORT="${E2E_HTTP_PORT:-18080}"
E2E_DB_PORT="${E2E_DB_PORT:-13306}"
E2E_BASE_URL="http://127.0.0.1:${E2E_HTTP_PORT}/openboxes"
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
  docker compose -p "$E2E_PROJECT" -f "$E2E_DIR/docker/compose.yml" "$@"
}

# compose for commands that do not (re)create containers: the variables the file interpolates only need a value.
compose_running() {
  E2E_WAR=/dev/null E2E_APP_CONFIG=/dev/null E2E_MY_CNF=/dev/null compose "$@"
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
