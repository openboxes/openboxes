#!/usr/bin/env bash
# =============================================================================
# OpenBoxes - Archive integration files on the SFTP server
# =============================================================================
#
# Moves the integration CSV files out of the working directories on the SFTP
# server into a timestamped archive folder, so a test environment can start a
# clean UAT cycle without old files being re-processed or confused with new
# runs. Files are MOVED server-side (a remote rename, no re-upload), so nothing
# is lost - they just move under the archive root. Optionally a local backup
# copy can be downloaded first.
#
# This is an infrastructure/ops utility, not part of the application. OpenBoxes
# core has no SFTP poller; file exchange is handled by external middleware, so
# the host, credentials and directories are environment-specific and must be
# provided via the environment variables below.
#
# REQUIREMENTS
#   lftp (https://lftp.yar.ru/) - the standard tool for scripted SFTP.
#     Debian/Ubuntu: apt-get install lftp    macOS: brew install lftp
#
# USAGE
#   Configure via environment variables, then run. DRY_RUN defaults to true so
#   the first run only prints what WOULD be archived.
#
#     SFTP_HOST=sftp.example.com \
#     SFTP_USER=vvg \
#     SFTP_KEY=~/.ssh/vvg_sftp \
#     SFTP_DIRS="/inbound /outbound" \
#     DRY_RUN=true \
#     ./archive-sftp-files.sh
#
#   When the dry-run output looks right, re-run with DRY_RUN=false.
#
# AUTH
#   Key-based (preferred): set SFTP_KEY to a private key path.
#   Password-based:        set SFTP_PASSWORD (consider a secrets manager; avoid
#                          leaving passwords in shell history).
#
# SAFETY
#   * DRY_RUN=true (default) changes nothing.
#   * Files are archived (moved), not deleted. Review/purge the archive root
#     separately once you are sure the files are no longer needed.
#   * The archive root should be on the same filesystem as the source dirs so
#     the server-side rename succeeds; otherwise set LOCAL_BACKUP_DIR and use
#     the download path instead.
# =============================================================================

set -euo pipefail

# --- configuration (override via environment) --------------------------------
SFTP_HOST="${SFTP_HOST:-}"
SFTP_PORT="${SFTP_PORT:-22}"
SFTP_USER="${SFTP_USER:-}"
SFTP_PASSWORD="${SFTP_PASSWORD:-}"          # optional; prefer key auth
SFTP_KEY="${SFTP_KEY:-}"                     # path to private key (optional)

# Space-separated list of remote directories whose files should be archived.
SFTP_DIRS="${SFTP_DIRS:-/inbound /outbound}"

# Glob of files to archive within each directory. Default targets CSV files;
# set to "*" to archive everything (subdirectories included).
ARCHIVE_GLOB="${ARCHIVE_GLOB:-*.csv}"

# Remote archive root. A per-run timestamped folder is created beneath it, and
# within that a subfolder per source directory.
ARCHIVE_ROOT="${ARCHIVE_ROOT:-/archive}"
TIMESTAMP="${TIMESTAMP:-$(date +%Y%m%d-%H%M%S)}"

# If set to a local path, download a backup copy of all matching files before
# moving them on the server.
LOCAL_BACKUP_DIR="${LOCAL_BACKUP_DIR:-}"

# Safe default: only report what would happen.
DRY_RUN="${DRY_RUN:-true}"
# -----------------------------------------------------------------------------

if ! command -v lftp >/dev/null 2>&1; then
    echo "ERROR: lftp is required but not installed." >&2
    exit 1
fi

for var in SFTP_HOST SFTP_USER; do
    if [[ -z "${!var}" ]]; then
        echo "ERROR: $var must be set." >&2
        exit 1
    fi
done

# Build the lftp "open" preamble (auth + connection settings).
lftp_open() {
    echo "set sftp:auto-confirm yes"
    echo "set net:max-retries 2"
    echo "set net:timeout 20"
    if [[ -n "$SFTP_KEY" ]]; then
        echo "set sftp:connect-program \"ssh -a -x -i ${SFTP_KEY}\""
        echo "open -u ${SFTP_USER}, -p ${SFTP_PORT} sftp://${SFTP_HOST}"
    else
        echo "open -u ${SFTP_USER},${SFTP_PASSWORD} -p ${SFTP_PORT} sftp://${SFTP_HOST}"
    fi
}

# List files matching ARCHIVE_GLOB in a remote directory (basenames, one/line).
list_files() {
    local dir="$1"
    { lftp <<-EOF 2>/dev/null
		$(lftp_open)
		cd "${dir}"
		cls -1 ${ARCHIVE_GLOB}
		bye
	EOF
    } | sed 's#.*/##' | sed '/^$/d' || true
}

echo "=== SFTP archive ==="
echo "Host:         ${SFTP_USER}@${SFTP_HOST}:${SFTP_PORT}"
echo "Source dirs:  ${SFTP_DIRS}"
echo "Glob:         ${ARCHIVE_GLOB}"
echo "Archive root: ${ARCHIVE_ROOT}/${TIMESTAMP}"
[[ -n "$LOCAL_BACKUP_DIR" ]] && echo "Local backup: ${LOCAL_BACKUP_DIR}/${TIMESTAMP}"
[[ "$DRY_RUN" == "true" ]] && echo "Mode:         DRY RUN (no changes)" || echo "Mode:         LIVE"
echo

total=0
for dir in $SFTP_DIRS; do
    base="$(basename "$dir")"
    archive_dir="${ARCHIVE_ROOT}/${TIMESTAMP}/${base}"

    mapfile -t files < <(list_files "$dir")
    count="${#files[@]}"
    echo "--- ${dir} (${count} file(s)) -> ${archive_dir}"

    if [[ "$count" -eq 0 ]]; then
        continue
    fi

    total=$((total + count))

    if [[ "$DRY_RUN" == "true" ]]; then
        for f in "${files[@]}"; do
            echo "    would archive: ${f}"
        done
        continue
    fi

    # Optional local backup copy before moving.
    if [[ -n "$LOCAL_BACKUP_DIR" ]]; then
        local_dir="${LOCAL_BACKUP_DIR}/${TIMESTAMP}/${base}"
        mkdir -p "$local_dir"
        lftp <<-EOF
			$(lftp_open)
			lcd "${local_dir}"
			cd "${dir}"
			$(for f in "${files[@]}"; do echo "get \"${f}\""; done)
			bye
		EOF
    fi

    # Create the archive directory and move each matching file into it.
    lftp <<-EOF
		$(lftp_open)
		mkdir -p "${archive_dir}"
		$(for f in "${files[@]}"; do echo "mv \"${dir}/${f}\" \"${archive_dir}/${f}\""; done)
		bye
	EOF

    echo "    archived ${count} file(s)"
done

echo
if [[ "$DRY_RUN" == "true" ]]; then
    echo "=== DRY RUN complete. ${total} file(s) would be archived. Re-run with DRY_RUN=false. ==="
else
    echo "=== Done. Archived ${total} file(s) to ${ARCHIVE_ROOT}/${TIMESTAMP}. ==="
fi
