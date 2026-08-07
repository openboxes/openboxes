# sftp

Helpers for maintaining SFTP integration drops — the folders where an
upstream system deposits files that OpenBoxes then ingests.

## Scripts

| Script | Purpose |
| --- | --- |
| `archive-sftp-files.sh` | Walk one or more remote SFTP directories, download files, then move each file into an `archive/` subdirectory on the remote host so it isn't re-processed. Supports dry-run, key or password auth, and recursive walking. |

## Prerequisites

- `lftp` installed locally.
- SFTP account on the remote host — key-based auth strongly preferred.

## Usage

Configuration is via environment variables so credentials stay out of
shell history and version control:

```
SFTP_HOST=sftp.example.com \
SFTP_USER=sftpuser \
SFTP_KEY=~/.ssh/sftp_key \
SFTP_DIRS="/data/integration" \
DRY_RUN=true \
tools/sftp/archive-sftp-files.sh
```

Required: `SFTP_HOST`, `SFTP_USER`, `SFTP_DIRS` (space-separated remote parent
directories to walk).

Auth: set either `SFTP_KEY` (path to a private key — preferred) or
`SFTP_PASSWORD`. Passwords are supported but discouraged; consider a
secrets manager rather than exporting them in your shell.

Optional: `SFTP_PORT` (default `22`), `DRY_RUN=true` (list what would
happen without downloading or moving anything), `RECURSIVE=true` (walk
subdirectories as well).

## Safety

- `DRY_RUN=true` is the recommended first pass for any new host or directory.
- Files are **moved** into `archive/`, not deleted, so a mistake can be
  recovered by moving them back.
- The script never overwrites existing local files; a name collision aborts
  the operation for that file.
