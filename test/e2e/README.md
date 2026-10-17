# End-to-end suite (test/e2e)

Playwright tests that run against a real OpenBoxes built from this checkout, in a sealed Docker stack:

- **mobile-api** (`tests/mobile-api`): one or more tests per entry of the mobile API contract (every
  call the openboxes-mobile app makes, with the response fields it reads), made the way the app makes
  them (sign-in, session cookie, axios headers). A coverage report says which entries were exercised
  and which pass.
- **flows** (`tests/flows`): warehouse flows end to end, mixing the browser, the API and the handheld
  calls: receive and put away; pick, pack and ship; sortation, cross-dock and backorder.
- **setup** (`tests/setup`): seeds the data the API creates and saves the browser sign-in; the other
  projects depend on it.

Tests that assert the correct behaviour of something that does not work yet are `knownGap(...)`
(`test.fixme`) with a self-contained description of the symptom; `--known-gaps` runs them as normal
tests, so a fixed gap shows up as a pass.

This suite is **not run by CI yet**: run it locally. It is unrelated to the repository's older
`test/unit`, `test/integration` and `test/data` folders.

## Prerequisites

- Docker with `docker compose` (the stack: MariaDB 11.4 or MySQL 8.0.36, the app on
  `eclipse-temurin:8-jre-jammy`, a Python label stub, a socat gateway).
- JDK 8 for the WAR build: `OBAF_JAVA_HOME` (or `JAVA_HOME`) pointing at a JDK 8 home.
- Node >= 20 (`NODE_HOME`, `node` on `PATH`, or the newest nvm v22 install). The runner installs
  `node_modules` (npm ci) once and runs `playwright install chromium` on every run (a no-op once the
  browser is installed).
- Python 3 (the CDN mirror tool and the run summary).
- Two resource files that come from the backend test suite (added to the repository by a separate
  change; until it is merged they are missing):
  - `src/test/resources/regression-testcontainers/my.cnf`: the database server config the stack
    mounts (utf8mb3, UTC, sql_mode). **Needed by the stack**: without it nothing starts.
  - `src/integration-test/resources/mobile-contract/mobile-contract.json`: the mobile API contract.
    **Needed only by the mobile-api project and its coverage report** (and by the handheld steps of
    the flows, which make contract calls). It is read on first use, so setup, the rest of the flows
    and the web UAT run without it; what needs it fails with a message naming the expected path.

### Caches and credentials

- The tools use the usual per-user caches outside the repository: `~/.gradle` (Gradle), the npm cache,
  Playwright's browser cache, and `~/.cache/openboxes-e2e` (the WAR build clone, below).
- The test databases use fixed throwaway credentials (`root`/`root`, `openboxes`/`openboxes`, and the
  web UAT's SELECT-only `uat_audit` user). They are not secrets: the database lives in a tmpfs inside
  the stack, is published on 127.0.0.1 only, and is gone when the stack stops.

## Running

From the repository root:

```sh
export OBAF_JAVA_HOME=/path/to/jdk8           # or JAVA_HOME
test/e2e/cdn-mirror/fetch.sh                  # once: fill the CDN mirror (uses the internet)
test/e2e/run.sh                               # build the WAR, up, seed, test, down
```

`run.sh` does everything: it fetches the CDN mirror when it is missing, builds the WAR once per HEAD
commit, brings the stack up, seeds it, runs Playwright and tears the stack down. Step by step instead:

```sh
test/e2e/build-war.sh                         # build/e2e/war/openboxes-<HEAD sha>.war (--force rebuilds)
test/e2e/up.sh                                # start the stack and seed it (--no-seed to skip)
test/e2e/run.sh --reuse --project mobile-api  # test against the running stack
test/e2e/down.sh                              # stop it
```

Options of `run.sh`: `--project mobile-api|flows|all`, `--keep-up`, `--reuse`, `--known-gaps`, and
`-- <playwright args>` (for example `-- --grep "putaway"`). Environment: `E2E_HTTP_PORT` (18080),
`E2E_DB_PORT` (13306), `E2E_LABEL_PORT` (18081), `E2E_DB_IMAGE` (`mariadb:11.4`, or `mysql:8.0.36`),
`E2E_WORKERS` (2), `E2E_STRICT_EGRESS=1` (fail a test whose pages requested a URL the mirror lacks).

Reports go to `build/e2e/reports/`: `junit.xml`, `html/` (traces of failures), `contract-coverage.json`,
`cdn-mirror.{json,txt}`, `app.log` (the app container log) and `summary.txt`. The exit status is
Playwright's.

### The WAR

The WAR is built from **HEAD, the committed tree**, never from uncommitted changes: commit a change
before testing it. `build-war.sh` keeps a `git clone --shared` of this checkout outside the repository,
in `~/.cache/openboxes-e2e/<checkout id>` (`E2E_BUILD_DIR` overrides the location), resets it to HEAD,
cleans it (`git clean -fdx`, so no output of an earlier build reaches the WAR), runs
`./gradlew assemble -x test -x integrationTest` there and copies the result to
`build/e2e/war/openboxes-<HEAD sha>.war`. Later runs at the same commit reuse that WAR; `--force`
rebuilds HEAD.

The clone carries a marker file (`.openboxes-e2e-build`) the script writes when it creates it; a
directory at that location that is not empty and has no marker is never deleted or reset (the script
stops with a message instead). A lock directory next to the clone (`<clone>.lock`) keeps two builds of
the same clone apart: a second build waits (up to `E2E_BUILD_LOCK_TIMEOUT` seconds, default 3600).

## The sealed stack

No test reaches the internet:

- `db`, `app` and the label stub sit on an internal Docker network with no route out; the gateway
  container only forwards three loopback ports of the host (HTTP, database, label stub).
- The app runs in production mode (`grails.env=prod`) with `docker/openboxes.yml`: mail, webhooks and
  third-party widgets are off, polling jobs are parked, and the Zebra printer and the labelary render
  point at the label stub (`docker/labelstub.py`), which records what it received for the tests.
- The browser is sealed twice. Every browser context is routed through `lib/ts/cdn-mirror.ts`:
  only the app's port and the label stub's port on `127.0.0.1`/`localhost` continue (any other local
  port is aborted and reported as BLOCKED), third-party CDN assets come from the CDN mirror (below),
  everything else is aborted. Below that, Chromium starts with
  `--host-resolver-rules=MAP * ~NOTFOUND, EXCLUDE 127.0.0.1, EXCLUDE localhost` (both Playwright
  configs), so no other host name resolves, even for requests the routing does not see.
- The API clients refuse any URL but the app's and the label stub's loopback ports.

## CDN mirror

OpenBoxes' server-rendered pages load scripts, styles and fonts from public CDNs. The sealed browser
has no internet, so `cdn-mirror/fetch.sh` builds a byte-exact copy of every one of them in
`cdn-mirror/cache/` (ignored by git, never committed), and `lib/ts/cdn-mirror.ts` serves it to the
browser in place of the CDN: the pages run with their libraries, as production browsers see them.

- `fetch.sh` scans this checkout's HEAD commit (the committed tree the WAR is built from; `--sha <rev>`
  scans another commit) for every third-party URL the templates make a browser load (`grails-app/views/**`, every `*.gsp`, the taglibs),
  downloads each into `cache/<host>/<path>`, follows `url(...)` and `@import` in CSS, verifies every
  SRI `integrity=` hash, and writes `cache/manifest.json`. Incremental; `--refresh` rebuilds from
  scratch; `--check` only warns about URLs the mirror lacks (the runners call it on every run, and
  fetch the mirror when it is missing).
- The tool fetches only `http`/`https` URLs of hosts that resolve to public addresses (checked again on
  every redirect and for every URL a CSS file references), stores them under `cache/<host>/<path>`
  only, and writes or deletes (`--refresh`) only in a `cache/` it made (marker `.openboxes-cdn-mirror`).
- `exclude.json` lists third-party services OpenBoxes loads only when configured (analytics, captcha,
  feedback widgets, in-context translation): they are not mirrored and the browser aborts them.
- A URL missing from the mirror is aborted and reported per page in `cdn-mirror.txt`, not failed.

### Licences

Every mirrored file is a third-party, redistributable open-source asset, copied unmodified, and keeps
the licence of its project:

| Library (as OpenBoxes references it) | Hosts | Licence |
|---|---|---|
| jQuery 1.7.2, 1.8.2, 3.3.1 slim, 3.5.1 slim | cdnjs.cloudflare.com, ajax.aspnetcdn.com, code.jquery.com | MIT (jquery.org/license) |
| jQuery UI 1.8.2, 1.8.24 (+ smoothness theme images) | cdnjs.cloudflare.com, ajax.googleapis.com | MIT (1.8.x: dual MIT or GPL v2) |
| select2 4.0.13 | cdn.jsdelivr.net | MIT |
| DataTables 1.9.4 | cdnjs.cloudflare.com, ajax.aspnetcdn.com | GPL v2 or BSD (3-clause), at the user's choice |
| DataTables 1.10.0, Bootstrap 3 integration plug-in | cdn.datatables.net | MIT |
| Notify.js 0.4.2 | cdnjs.cloudflare.com | MIT |
| Bootstrap 3.1.1 (incl. Glyphicons Halflings font), 4.3.1, 5.0.0-beta2 | netdna.bootstrapcdn.com, cdn.jsdelivr.net | MIT |
| Popper.js 1.14.7 | cdn.jsdelivr.net | MIT |
| Font Awesome 3.2.1 | netdna.bootstrapcdn.com | font SIL OFL 1.1, code MIT |
| Font Awesome Free 5.14.0, 5.15.2 | use.fontawesome.com, cdnjs.cloudflare.com | icons CC BY 4.0, fonts SIL OFL 1.1, code MIT |
| Remix Icon 2.5.0 | cdn.jsdelivr.net | Apache 2.0 |
| YUI 2.9.0 reset-fonts-grids | cdnjs.cloudflare.com | BSD (3-clause) |
| PivotTable.js 1.6.3 | cdnjs.cloudflare.com | MIT |
| jqGrid 4.6.0 | cdnjs.cloudflare.com | dual MIT or GPL |
| Moment.js 2.25.3 (cdnjs), 2.18.1 (jsdelivr `momentjs/latest`) | cdnjs.cloudflare.com, cdn.jsdelivr.net | MIT |
| jquery-rss 4.3.0 | cdnjs.cloudflare.com | MIT |
| jQuery Date Range Picker 0.16.1 | cdnjs.cloudflare.com | MIT |
| Inter (Google Fonts CSS and woff2 files) | fonts.googleapis.com, fonts.gstatic.com | SIL OFL 1.1 |

Not mirrored (see `cache/manifest.json`): the `excluded` services above, and `failed` URLs the upstream
no longer serves; those answer 404 in production too, and the route handler answers them 404 the same
way.

## Layout

| Path | What |
|---|---|
| `run.sh`, `build-war.sh`, `up.sh`, `seed.sh`, `down.sh`, `lib/common.sh` | the runners |
| `docker/` | the compose file, the app config, the label stub |
| `fixtures/seed.sql` | reference data no API creates (category, event types, ZPL label templates) |
| `lib/ts/` | the Playwright `test` (sealed browser, fixtures), the contract checks, SQL fixtures, reporters |
| `tests/` | the `setup`, `mobile-api` and `flows` projects |
| `cdn-mirror/` | `fetch.sh`, `mirror.py`, `exclude.json`; the mirror itself in `cache/` (not committed) |
