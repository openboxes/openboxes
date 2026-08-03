#!/usr/bin/env bash
#
# Drop, recreate, and import a mysqldump into a local MariaDB / MySQL
# database. Accepts either an uncompressed .sql or a gzipped .sql.gz.
#
# Assumes:
#   * ~/.my.cnf on the LOCAL machine holds credentials with rights to
#     DROP / CREATE the target database and GRANT to the app user.
#
# Usage:
#   restore-local-dump.sh <dump-file> [target-db]
#
# Defaults:
#   target-db = vvg_latest

set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <dump-file> [target-db]" >&2
  exit 2
fi

DUMP_FILE="$1"
TARGET_DB="${2:-vvg_latest}"
APP_USER="openboxes"
APP_HOST="localhost"

if [[ ! -f "${DUMP_FILE}" ]]; then
  echo "Dump file not found: ${DUMP_FILE}" >&2
  exit 1
fi

echo "About to DROP and recreate database '${TARGET_DB}' and import ${DUMP_FILE}."
read -p "Proceed? y/N: " CONFIRMATION
if [[ ${CONFIRMATION} != "y" && ${CONFIRMATION} != "Y" ]]; then
  echo "Aborted."
  exit 0
fi

echo "Recreating ${TARGET_DB}..."
mysql -e "DROP DATABASE IF EXISTS \`${TARGET_DB}\`;
          CREATE DATABASE \`${TARGET_DB}\` DEFAULT CHARSET utf8mb4;
          GRANT ALL ON \`${TARGET_DB}\`.* TO '${APP_USER}'@'${APP_HOST}';"

echo "Importing (start: $(date +%T))..."
START=$(date +%s)
if [[ "${DUMP_FILE}" == *.gz ]]; then
  gunzip -c "${DUMP_FILE}" | mysql "${TARGET_DB}"
else
  mysql "${TARGET_DB}" < "${DUMP_FILE}"
fi
ELAPSED=$(( $(date +%s) - START ))

echo "Import finished in ${ELAPSED}s."
echo "Sentinel row counts:"
mysql "${TARGET_DB}" -e "
  SELECT 'location'    AS tbl, COUNT(*) AS rows_ FROM location
  UNION ALL SELECT 'product',      COUNT(*) FROM product
  UNION ALL SELECT 'requisition',  COUNT(*) FROM requisition
  UNION ALL SELECT 'shipment',     COUNT(*) FROM shipment
  UNION ALL SELECT '\`order\`',    COUNT(*) FROM \`order\`;"
