#!/usr/bin/env bash
# Build the executable OpenBoxes WAR (React bundle included) from this checkout, once per commit.
#
# usage: test/e2e/build-war.sh [--force]
#
#   --force   rebuild even when the WAR of HEAD already exists
#
# The WAR is built from HEAD, in a `git clone --shared` of this checkout (so the build sees exactly
# what is committed, also from a linked git worktree) kept OUTSIDE the repository, in
# ${E2E_BUILD_DIR:-~/.cache/openboxes-e2e/<checkout id>} (inside it, the bundle's eslint step would
# also read this repository's .eslintrc), with
# `./gradlew assemble -x test -x integrationTest`, and copied to build/e2e/war/openboxes-<HEAD sha>.war,
# which later runs reuse. Commit a change before testing it.
#
# The build clone carries a marker file (.openboxes-e2e-build) written when this script creates it.
# Before each build it is reset to the commit and cleaned (`git clean -fdx`, so no earlier build or
# frontend output reaches the WAR). A directory that exists, is not empty and lacks the marker is never
# touched: the script stops instead. A lock directory next to the clone (<clone>.lock) keeps two builds
# of the same clone apart; a second build waits for it (E2E_BUILD_LOCK_TIMEOUT seconds, default 3600).
#
# Needs a JDK 8 (OBAF_JAVA_HOME or JAVA_HOME). The build itself may use the network (Gradle and
# npm dependencies, the Gradle node plugin's Node download); the running app and the tests never do.
# Prints the WAR path on stdout.
set -euo pipefail
E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "$E2E_DIR/lib/common.sh"

FORCE=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --force) FORCE=1; shift ;;
    -h|--help) sed -n '2,23p' "$0"; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done

FULL="$(head_sha)"
OUT="$(war_path "$FULL")"
if [[ -n "$(git -C "$REPO_ROOT" status --porcelain --untracked-files=no -- . ':(exclude)test')" ]]; then
  log "note: uncommitted changes are not in the WAR; it is built from HEAD ($FULL)"
fi
if [[ -f "$OUT" && $FORCE -eq 0 ]]; then
  log "WAR for $FULL already built: $OUT (--force rebuilds it)"; echo "$OUT"; exit 0
fi

JAVA_HOME="$(java8_home)"; export JAVA_HOME
SRC="${E2E_BUILD_DIR:-${XDG_CACHE_HOME:-$HOME/.cache}/openboxes-e2e/$(printf '%s' "$REPO_ROOT" | cksum | cut -d' ' -f1)}"
SRC="${SRC%/}"
MARKER=.openboxes-e2e-build
LOCK="$SRC.lock"
LOCK_TIMEOUT="${E2E_BUILD_LOCK_TIMEOUT:-3600}"

# One build per clone at a time: mkdir is atomic, the EXIT trap removes the lock.
mkdir -p "$(dirname "$SRC")"
waited=0
until mkdir "$LOCK" 2>/dev/null; do
  if [[ $waited -eq 0 ]]; then
    log "another build holds $LOCK (pid $(cat "$LOCK/pid" 2>/dev/null || echo '?')); waiting up to $LOCK_TIMEOUT s"
  fi
  if [[ $waited -ge $LOCK_TIMEOUT ]]; then
    echo "timed out after $LOCK_TIMEOUT s waiting for the build lock $LOCK; if no build is running, remove it (rm -r $LOCK) and retry" >&2
    exit 1
  fi
  sleep 5; waited=$(( waited + 5 ))
done
echo "$$" > "$LOCK/pid"
# shellcheck disable=SC2329 # invoked by the EXIT trap
release_lock() { rm -f "$LOCK/pid"; rmdir "$LOCK" 2>/dev/null || true; }
trap release_lock EXIT

# Another build may have produced the WAR while this one waited.
if [[ -f "$OUT" && $FORCE -eq 0 ]]; then
  log "WAR for $FULL already built: $OUT"; echo "$OUT"; exit 0
fi

if [[ -d "$SRC/.git" && -f "$SRC/$MARKER" ]]; then
  : # the build clone this script created
elif [[ -e "$SRC" && ! -d "$SRC" ]]; then
  echo "$SRC exists and is not a directory: set E2E_BUILD_DIR to another path, or remove it" >&2
  exit 1
elif [[ -d "$SRC" && -n "$(ls -A "$SRC")" ]]; then
  echo "$SRC exists, is not empty and is not a build clone of this script (no $MARKER in it);" \
    "it is left alone: set E2E_BUILD_DIR to another path, or remove it yourself" >&2
  exit 1
else
  log "cloning HEAD into $SRC"
  git clone --quiet --shared --no-checkout "$REPO_ROOT" "$SRC"
  echo "build clone of $REPO_ROOT for test/e2e/build-war.sh; safe to delete" > "$SRC/$MARKER"
fi
git -C "$SRC" fetch --quiet "$REPO_ROOT" "$FULL"
git -C "$SRC" checkout --quiet --force --detach "$FULL"
# No output of an earlier build (build/, the React bundle, generated sources) may reach this WAR.
# node_modules goes too; the Gradle node plugin installs it again.
git -C "$SRC" clean --quiet -fdx -e "$MARKER"
log "building the WAR at $FULL with JAVA_HOME=$JAVA_HOME (one Gradle build; ~10 min cold)"
start=$(date +%s)
# assemble = the bootRepackage'd executable WAR; processResources runs the React bundle the browser
# flows need. No tests.
(cd "$SRC" && ./gradlew --no-daemon -Dorg.gradle.jvmargs=-Xmx3g assemble -x test -x integrationTest >&2)
mkdir -p "$(dirname "$OUT")"
cp "$SRC/build/libs/openboxes.war" "$OUT"
log "built $OUT in $(( $(date +%s) - start )) s"
echo "$OUT"
