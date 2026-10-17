#!/usr/bin/env bash
# Build test/e2e/cdn-mirror/cache: OpenBoxes' third-party CDN assets, byte-exact, for the sealed browser
# tests (test/e2e/README.md "CDN mirror"). This one-time fetch uses the internet; the tests never do.
#
# usage: test/e2e/cdn-mirror/fetch.sh [--refresh] [--sha <rev>]
#        test/e2e/cdn-mirror/fetch.sh --check [--sha <rev>]
#
#   (none)     fetch every URL the templates reference that the mirror lacks (incremental)
#   --refresh  rebuild the mirror from scratch
#   --check    warn (exit 0) for every third-party URL of the templates missing from the manifest
#   --sha      scan that commit instead of HEAD
#
# Source: this checkout's HEAD commit, read with git show (the committed tree, which is what
# test/e2e/build-war.sh builds the WAR from; uncommitted template changes are not scanned). The
# manifest records the commit. The mirror is not committed (cache/ is ignored by git).
# Exit 1 when a template's SRI integrity= hash does not match the stored bytes.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$HERE/../../.." && pwd)"

MODE=fetch; REFRESH=(); REV=(--sha "$(git -C "$REPO_ROOT" rev-parse 'HEAD^{commit}')")
while [[ $# -gt 0 ]]; do
  case "$1" in
    --refresh) REFRESH=(--refresh); shift ;;
    --check) MODE=check; shift ;;
    --sha) REV=(--sha "$(git -C "$REPO_ROOT" rev-parse "$2^{commit}")"); shift 2 ;;
    -h|--help) sed -n '2,16p' "$0"; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done
exec python3 -I "$HERE/mirror.py" "$MODE" --src "$REPO_ROOT" ${REV[@]+"${REV[@]}"} --out "$HERE/cache" ${REFRESH[@]+"${REFRESH[@]}"}
