---
description: Project-level verification runbook for the OpenBoxes EST fork — compile, test, lint, and custom-path isolation checks against the real stack (Grails 3.3.16 / JDK 8 / Node 14).
---

# /verify — OpenBoxes pre-commit verification

This is the **project-local** `/verify` runbook. Run it before committing or opening a PR. It **replaces** the generic global `verify` skill, which doesn't know about our Grails / Groovy / Node 14 stack.

The real project scripts are defined in `package.json` and `build.gradle`. Do not invent new tasks — the ones below are the ones that actually exist.

## Step 1 — Backend compile (fast smoke test)

This is the single highest-signal check. It catches most bad edits in seconds to a minute.

```bash
./gradlew compileGroovy
```

- Must succeed before proceeding. If it fails, hand the error to the `java-build-resolver` agent.
- Also compiles `src/main/java/` helpers first (compileJava runs in the dependency chain).

## Step 2 — Backend unit tests (Spock specs)

```bash
./gradlew test
```

Runs the Spock unit specs under `src/test/groovy/`. For new code this is a blocker — all new specs should land under `src/test/groovy/org/pih/warehouse/custom/<feature>/`.

## Step 3 — Backend integration tests (slow, authoritative)

```bash
./gradlew integrationTest
```

Runs the `@Integration` specs under `src/integration-test/groovy/` with a real Grails context. Much slower than `test` — skip during inner-loop iteration, but **do not skip before PR**. It's the only layer that catches GORM / transactional / Liquibase wiring bugs.

## Step 4 — Frontend lint

```bash
npm run eslint
```

> The `package.json` script is named **`eslint`**, not `lint` — `npm run eslint` runs `eslint --fix --ext .js,.jsx ./src/js/`.

## Step 5 — Frontend tests

```bash
npm test
```

Runs Jest + @testing-library/react. Required for any change under `src/js/`.

## Step 6 — Custom-path isolation check

Every **newly added** `.groovy`, `.java`, `.jsx`, `.js`, `.scss` file must live under a `custom/` path — see `rules/custom-package-isolation.md`.

> **⚠️ Pick the base ref first.** EST feature branches are typically based on `release/est/<version>`, not `develop`. If you don't set `VERIFY_BASE`, the default `origin/develop` is likely wrong for your branch and you'll see false positives (files from the EST release branch that aren't "new" to your feature). Run `git merge-base HEAD origin/develop origin/release/est/*` to find the right base, then export it.

```bash
# Set this to the ref your feature branched FROM, not the ref you'll merge INTO:
export VERIFY_BASE="${VERIFY_BASE:-origin/release/est/0.9.7}"   # current release branch
# export VERIFY_BASE=origin/develop                              # only if you branched from develop

git fetch origin "${VERIFY_BASE#origin/}" 2>/dev/null || true

git diff --name-only --diff-filter=A "${VERIFY_BASE}...HEAD" -- \
    grails-app/ src/main/groovy/ src/main/java/ src/js/ \
    src/test/groovy/ src/integration-test/groovy/ \
  | grep -E '\.(groovy|java|js|jsx|scss)$' \
  | grep -vE '(^|/)custom/' \
  || echo "OK: all newly added source files are under custom/"
```

- Any output from the final `grep -vE` stage is a **blocker** — re-route the file to a `custom/` path (see `rules/custom-package-isolation.md` for the layer → path table).
- Migrations under `grails-app/migrations/` must specifically be under `grails-app/migrations/custom/`; the pipeline above catches that too.
- Simpler top-level pathspecs (`grails-app/`, `src/js/`, …) are used instead of `**` globs because git's default pathspec handling does not expand `**` unless `--glob-pathspecs` is set — the extension filter in the next stage does the narrowing.

## Step 7 — Pre-Commit Self-Review (manual)

Open `CLAUDE.md` (repo root) and walk the **Pre-Commit Self-Review — Project-Specific** and team-wide checklists. They cover upstream-compatibility isolation, Grails layering, Groovy idioms, language floors, `@Autowired` absence, raw-SQL justification, and the Boy Scout exemption — the things an automated check can't verify for you.

## When a step fails

| Step | Failing — who to ask |
|---|---|
| `compileGroovy` / `test` / `integrationTest` | `java-build-resolver` agent for errors, `java-reviewer` agent for code quality. |
| `npm run eslint` / `npm test` | `react-reviewer` agent. |
| Custom-path isolation | Move the file under `custom/`. No agent — it's a hard rule (`rules/custom-package-isolation.md`). |

## Reference

- Stack / command details: `.claude/commands/gradle-build.md`
- Upstream-isolation rule: `.claude/rules/custom-package-isolation.md`
- Backend review criteria: `.claude/agents/java-reviewer.md`
- Frontend review criteria: `.claude/agents/react-reviewer.md`
