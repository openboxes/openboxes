#!/usr/bin/env bash
#
# Stream a mysqldump of the vvg openboxes database over SSH to a
# timestamped, gzipped file on the local machine.
#
# Assumes:
#   * SSH access to the remote host (key-based, no password prompt).
#   * ~/.my.cnf on the REMOTE host holds credentials for `mysqldump`.
#
# Usage:
#   fetch-vvg-dump.sh [-h remote-host] [-d remote-db] [-o output-dir]
#
# Defaults:
#   -h vvg.openboxes.com
#   -d openboxes
#   -o current directory

set -euo pipefail

REMOTE_HOST="vvg.openboxes.com"
REMOTE_DB="openboxes"
OUTPUT_DIR="."

while getopts ":h:d:o:" opt; do
  case "${opt}" in
    h) REMOTE_HOST="${OPTARG}" ;;
    d) REMOTE_DB="${OPTARG}" ;;
    o) OUTPUT_DIR="${OPTARG}" ;;
    *) echo "Usage: $0 [-h remote-host] [-d remote-db] [-o output-dir]" >&2; exit 2 ;;
  esac
done

mkdir -p "${OUTPUT_DIR}"
STAMP=$(date +%Y%m%d_%H%M%S)
OUTPUT_FILE="${OUTPUT_DIR%/}/${REMOTE_DB}_${STAMP}.sql.gz"

echo "Dumping ${REMOTE_DB} from ${REMOTE_HOST} -> ${OUTPUT_FILE}"
echo "(streaming; this may take a while for large databases)"

ssh "${REMOTE_HOST}" \
  "mysqldump --single-transaction --quick --routines --triggers \
             --set-gtid-purged=OFF ${REMOTE_DB} | gzip -9" \
  > "${OUTPUT_FILE}"

SIZE=$(du -h "${OUTPUT_FILE}" | cut -f1)
echo "Done. ${OUTPUT_FILE} (${SIZE})"
