#!/usr/bin/env bash
#
# Stream a mysqldump of a remote database over SSH to a timestamped,
# gzipped file on the local machine. Server-agnostic: pass the host
# and database explicitly.
#
# Views are excluded (OpenBoxes recreates them at boot). MySQL-8-safe
# dump flags are applied so the output imports cleanly into MariaDB
# via restore-local-dump.sh.
#
# Assumes:
#   * SSH access to the remote host (key-based, no password prompt).
#   * ~/.my.cnf on the REMOTE host holds credentials for `mysql` and
#     `mysqldump`.
#
# Usage:
#   fetch-remote-dump.sh <remote-host> <remote-db> [-o output-dir]
#
# Example:
#   fetch-remote-dump.sh db.example.com openboxes -o ~/dumps

set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <remote-host> <remote-db> [-o output-dir]" >&2
  exit 2
fi

REMOTE_HOST="$1"
REMOTE_DB="$2"
shift 2

OUTPUT_DIR="."
while getopts ":o:" opt; do
  case "${opt}" in
    o) OUTPUT_DIR="${OPTARG}" ;;
    *) echo "Usage: $0 <remote-host> <remote-db> [-o output-dir]" >&2; exit 2 ;;
  esac
done

mkdir -p "${OUTPUT_DIR}"
STAMP=$(date +%Y%m%d_%H%M%S)
OUTPUT_FILE="${OUTPUT_DIR%/}/${REMOTE_DB}_${STAMP}.sql.gz"

echo "Dumping ${REMOTE_DB} from ${REMOTE_HOST} -> ${OUTPUT_FILE}"
echo "(streaming; this may take a while for large databases)"

# Enumerate BASE TABLEs only, so views are skipped (OpenBoxes recreates
# them at boot). Then dump with MySQL-8-safe flags so the output
# imports cleanly into MariaDB:
#   --column-statistics=0  MySQL 8 emits column-histogram inserts that
#                          MariaDB rejects.
#   --no-tablespaces       avoids PROCESS-privilege warnings and
#                          CREATE TABLESPACE stubs MariaDB doesn't want.
# (Collations like utf8mb4_0900_ai_ci are rewritten by restore-local-dump.sh
# at import time.)
ssh "${REMOTE_HOST}" "
  set -e
  TABLES=\$(mysql -Nse \"SELECT table_name FROM information_schema.tables \
                        WHERE table_schema='${REMOTE_DB}' AND table_type='BASE TABLE'\")
  mysqldump --single-transaction --quick --routines --triggers \
            --set-gtid-purged=OFF --column-statistics=0 --no-tablespaces \
            ${REMOTE_DB} \${TABLES} | gzip -9
" > "${OUTPUT_FILE}"

SIZE=$(du -h "${OUTPUT_FILE}" | cut -f1)
echo "Done. ${OUTPUT_FILE} (${SIZE})"
