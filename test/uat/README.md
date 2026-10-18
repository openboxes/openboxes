# Web user-acceptance harness (test/uat)

A harness for browser acceptance flows: a person-readable script per flow, driven through the screens
only, with evidence for every run (video, trace, a screenshot per named step) and read-only database
audits. It runs against the sealed stack of the end-to-end suite (`test/e2e`, see its README).

There are **no acceptance flows yet**: the harness ships with an infrastructure smoke test
(`web/tests/smoke.spec.ts`) that proves the stack, the sealed browser, the evidence settings and the
helpers work, so flows can be written on top.

This harness is **not run by CI yet**: run it locally. It is unrelated to the repository's older
`test/unit`, `test/integration` and `test/data` folders.

## Prerequisites

The same as `test/e2e/README.md`: Docker with `docker compose`, a JDK 8 (`OBAF_JAVA_HOME` or
`JAVA_HOME`) for the WAR build, Node >= 20 (`NODE_HOME` or `PATH`), Python 3, and the resource file
`src/test/resources/regression-testcontainers/my.cnf` from the backend test suite (the stack's database
config, added to the repository by a separate change; the stack cannot start until it is there). The
other backend resource, `mobile-contract.json`, is not needed here: only the e2e mobile-api project and
its report read it. `test/uat/web` uses `test/e2e`'s `node_modules` through a symlink the runner
creates.

The WAR is built from **HEAD** (the committed tree, in a build clone under
`~/.cache/openboxes-e2e/<checkout id>`, or `E2E_BUILD_DIR`), so commit a change before testing it;
`test/e2e/build-war.sh --force` rebuilds HEAD (`test/e2e/README.md` "The WAR"). The tools use the usual
per-user caches (`~/.gradle`, the npm cache, Playwright's browser cache, `~/.cache/openboxes-e2e`).
The test databases use fixed throwaway credentials (including the SELECT-only `uat_audit` user the
runner creates), published on 127.0.0.1 only.

## Running

From the repository root:

```sh
export OBAF_JAVA_HOME=/path/to/jdk8           # or JAVA_HOME
test/e2e/cdn-mirror/fetch.sh                  # once: fill the CDN mirror (uses the internet)
test/uat/run.sh                               # build the WAR, up, seed, run the web UAT, down
test/uat/run.sh --tag smoke --keep-up         # only the smoke, leave the stack running
test/uat/run.sh --unit                        # the harness's own unit tests (node --test; needs only Node)
```

Options: `--tag <name>` (tests tagged `@<name>`: a flow name or `smoke`), `--db mysql:8.0.36`,
`--known-gaps`, `--headed`, `--keep-up`, `--reuse`, `-- <playwright args>`.

The UAT stack uses its own compose project and ports, so it never touches an e2e stack running on the
default ones: HTTP 28080, database 23306, label stub 28081 (`E2E_HTTP_PORT`, `E2E_DB_PORT`,
`E2E_LABEL_PORT`, `E2E_PROJECT` override them). With `--keep-up`, stop it with the same variables:
`E2E_PROJECT=<name printed by the run> test/e2e/down.sh`.

Reports go to `build/uat/reports/`: `html/` (open `index.html`: every step screenshot, video and
trace), `junit.xml`, `summary.txt`, `cdn-mirror.{json,txt}`, `app.log`, `test-results/`. The exit
status is Playwright's.

## Writing a flow

```ts
import { expect, flow, test } from '../lib/uat';
import { pages } from '../lib/pages';
import { reads } from '../lib/audit-db';

test('receiving: a clerk receives a partial inbound', flow('receiving'), async ({ page, step, audit, data, startState }) => {
  // setup: the per-flow factory, before the first step only
  const depot = await data.depot('DC');
  // ... declare and read back the starting state, then drive the screens:
  await step('open the login page', async () => { await pages.login.open(page); });
  // ... and compare what the screens claimed with the database, read only:
  await audit('the stock arrived in the receiving bin', async (db) => { /* reads.onHandByBin(...) */ });
});
```

- `flow(name)` tags a flow test `@<name>`; `knownGap(name, symptom)` marks a flow test whose step does
  not work yet (not run unless `--known-gaps`); `smoke()` marks infrastructure checks.
- `step(name, fn)`: a named screen step with a full-page screenshot after it (and on failure).
- `audit(name, fn)` and `startState(facts)`: database reads through a SELECT-only user, a read-only
  session and a statement guard (`lib/sql-guard.js`); an audit never drives the page.
- `data`: unique depots, bins, products, stock and users for the flow (built on `test/e2e`'s fixtures),
  allowed only before the first step.
- `web/lib/pages.ts`: page objects, one per screen, so a UI change is one edit.

The browser is sealed exactly as in `test/e2e` ("The sealed stack"): only the app's and the label
stub's ports on 127.0.0.1/localhost, third-party CDN assets from the CDN mirror, everything else
aborted and reported; Chromium's host resolver resolves no other name
(`--host-resolver-rules=MAP * ~NOTFOUND, EXCLUDE 127.0.0.1, EXCLUDE localhost`).
