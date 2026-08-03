#!/usr/bin/env bash
#
# Point the locally-running OpenBoxes instance at a different MySQL /
# MariaDB database by rewriting the JDBC URL in its Grails config
# (default: ~/.grails/openboxes.yml).
#
# Only the "/<dbname>?" segment of jdbc:mysql://host:port/<dbname>?...
# is rewritten -- host, port, and query params are left untouched.
# The original file is copied to <config>.<timestamp>.bak first.
#
# You still need to restart the app afterwards.
#
# Usage:
#   switch-local-db.sh <new-db-name> [-c config-file]
#
# Defaults:
#   config-file = ~/.grails/openboxes.yml

set -euo pipefail

CONFIG_FILE="${HOME}/.grails/openboxes.yml"

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <new-db-name> [-c config-file]" >&2
  exit 2
fi

NEW_DB="$1"
shift

while getopts ":c:" opt; do
  case "${opt}" in
    c) CONFIG_FILE="${OPTARG}" ;;
    *) echo "Usage: $0 <new-db-name> [-c config-file]" >&2; exit 2 ;;
  esac
done

if [[ ! -f "${CONFIG_FILE}" ]]; then
  echo "Config file not found: ${CONFIG_FILE}" >&2
  exit 1
fi

CURRENT_URL=$(grep -Eo 'jdbc:mysql://[^"[:space:]]+' "${CONFIG_FILE}" | head -n1 || true)
if [[ -z "${CURRENT_URL}" ]]; then
  echo "No jdbc:mysql://... URL found in ${CONFIG_FILE}" >&2
  exit 1
fi

CURRENT_DB=$(printf '%s' "${CURRENT_URL}" | sed -E 's|jdbc:mysql://[^/]+/([^?]+).*|\1|')
if [[ -z "${CURRENT_DB}" ]]; then
  echo "Could not parse current DB name from URL: ${CURRENT_URL}" >&2
  exit 1
fi

if [[ "${CURRENT_DB}" == "${NEW_DB}" ]]; then
  echo "Config already points at '${NEW_DB}'. Nothing to do."
  exit 0
fi

echo "Config:  ${CONFIG_FILE}"
echo "Current: ${CURRENT_DB}"
echo "New:     ${NEW_DB}"
read -p "Proceed? y/N: " CONFIRMATION
if [[ ${CONFIRMATION} != "y" && ${CONFIRMATION} != "Y" ]]; then
  echo "Aborted."
  exit 0
fi

BACKUP="${CONFIG_FILE}.$(date +%Y%m%d_%H%M%S).bak"
cp -p "${CONFIG_FILE}" "${BACKUP}"
echo "Backup: ${BACKUP}"

sed -i -E "s|(jdbc:mysql://[^/\"]+/)${CURRENT_DB}(\?)|\1${NEW_DB}\2|g" "${CONFIG_FILE}"

NEW_URL=$(grep -Eo 'jdbc:mysql://[^"[:space:]]+' "${CONFIG_FILE}" | head -n1)
echo "Now:     ${NEW_URL}"
echo
echo "Restart the OpenBoxes app for the change to take effect."
