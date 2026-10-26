#!/usr/bin/env bash
#
# Take a local mysqldump of a database as a compressed baseline snapshot,
# so a test iteration can restore the exact starting state via
# restore-local-dump.sh instead of re-fetching from the remote server.
#
# Assumes:
#   * ~/.my.cnf on the LOCAL machine holds credentials for `mysqldump`.
#
# Usage:
#   snapshot-local.sh [source-db] [output-dir]
#
# Defaults:
#   source-db  = openboxes
#   output-dir = current directory

set -euo pipefail

SOURCE_DB="${1:-openboxes}"
OUTPUT_DIR="${2:-.}"

mkdir -p "${OUTPUT_DIR}"
STAMP=$(date +%Y%m%d_%H%M%S)
OUTPUT_FILE="${OUTPUT_DIR%/}/${SOURCE_DB}_baseline_${STAMP}.sql.gz"

echo "Snapshotting ${SOURCE_DB} -> ${OUTPUT_FILE}"
mysqldump --single-transaction --quick --routines --triggers \
          --set-gtid-purged=OFF "${SOURCE_DB}" | gzip -9 > "${OUTPUT_FILE}"

SIZE=$(du -h "${OUTPUT_FILE}" | cut -f1)
echo "Done. ${OUTPUT_FILE} (${SIZE})"
