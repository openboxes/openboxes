# data-tools

Shell helpers for moving OpenBoxes databases around a developer laptop:
fetching a remote dump, restoring it locally, snapshotting a baseline for
iterative testing, and repointing the running app at a different database.

Designed to pair with `../data-reset/` — pull a real dataset, iterate on a
reset script against it, restore the baseline between runs.

## Scripts

| Script | Purpose |
| --- | --- |
| `fetch-remote-dump.sh` | `mysqldump` a remote database over SSH into a timestamped `.sql.gz` on your laptop. Views excluded; MySQL-8-safe flags applied. |
| `restore-local-dump.sh` | Drop, recreate, and import a dump into a local MariaDB/MySQL database. Rewrites MySQL-8 collations on the fly so MariaDB accepts them. |
| `snapshot-local.sh` | Take a compressed baseline dump of a local DB so a test iteration can restore the exact starting state without re-fetching from the remote server. |
| `switch-local-db.sh` | Repoint the locally-running OpenBoxes instance at a different database by rewriting the JDBC URL in `~/.grails/openboxes.yml`. |

## Prerequisites

- `~/.my.cnf` on **both** the local and remote hosts, with credentials for
  `mysql` and `mysqldump`. Scripts never take passwords on the command line.
- Key-based SSH access to the remote host.
- A local MariaDB or MySQL service running (the scripts assume host installation,
  not Docker).

## Typical workflow

```
# 1. Pull a fresh dump from the remote server.
tools/data-tools/fetch-remote-dump.sh db.example.com openboxes -o ~/dumps

# 2. Import it into a local database (drops + recreates the target DB first).
tools/data-tools/restore-local-dump.sh ~/dumps/openboxes_YYYYMMDD_HHMMSS.sql.gz openboxes

# 3. Freeze the freshly-imported state as a baseline so you can
#    reset between test iterations without re-fetching.
tools/data-tools/snapshot-local.sh openboxes ~/dumps

# 4. Point the running OpenBoxes app at the imported DB
#    (backs up ~/.grails/openboxes.yml first).
tools/data-tools/switch-local-db.sh openboxes
# ...restart the app...

# --- per test iteration ---
# run verify + reset script from ../data-reset/, inspect results, then:
tools/data-tools/restore-local-dump.sh ~/dumps/openboxes_baseline_<ts>.sql.gz openboxes
```

## MySQL 8 → MariaDB notes

`restore-local-dump.sh` rewrites `utf8mb4_0900_*` collations
(MySQL-8-only) to `utf8mb4_unicode_ci` on the way in — fixes the
`ERROR 1273 (HY000): Unknown collation: 'utf8mb4_0900_ai_ci'` error
without needing to re-dump.

`fetch-remote-dump.sh` also strips views (enumerates `BASE TABLE` from
`information_schema`) since OpenBoxes recreates them at boot, and passes
`--column-statistics=0 --no-tablespaces` so mysqldump 8's default output
imports cleanly into MariaDB.
