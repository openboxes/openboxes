# OpenBoxes — Project Guide

OpenBoxes is an open-source Logistics Management Information System (LMIS) built by Partners In Health for managing supply chains in healthcare and humanitarian settings.

## Upstream Compatibility (CRITICAL)

This is a **fork** of the upstream OpenBoxes project. We need to regularly pull updates from upstream, so **all new features and modifications must be as isolated and minimally invasive as possible** to keep merges seamless.

**Rules for every change:**

1. **Prefer new files over edits to existing files.** Put new services, controllers, domain classes, React components, and utilities in new files rather than extending existing ones. When you must touch upstream files, keep edits surgical and localized.
2. **Isolate feature code in dedicated packages/folders.** Place custom backend code under a clearly-named sub-package (e.g. `org.pih.warehouse.custom.<feature>/`) and custom frontend code under a dedicated folder (e.g. `src/js/custom/<feature>/`) whenever feasible.
3. **Hook in via extension points, not rewrites.** Favor Grails service injection, event listeners, taglib additions, and React component composition over modifying existing logic in place.
4. **Avoid reformatting or refactoring upstream files.** Do not change indentation, imports order, or rename symbols in files you didn't need to modify functionally — every incidental edit becomes a future merge conflict.
5. **Keep migrations additive.** New Liquibase changesets go in a dedicated custom folder or clearly-prefixed files so they don't collide with upstream changelog updates. Never edit existing upstream changesets.
6. **Isolate config overrides.** Prefer `external config` / environment-specific overrides over editing `application.yml` or `application.groovy` directly.
7. **Document the touch points.** When a change *must* modify upstream files, note the file(s) and the reason in the OpenSpec change's `design.md` so future merges know where to look.
8. **The Boy Scout Rule from the global CLAUDE.md is suspended for upstream files.** Do not clean up unrelated code in upstream files — it creates merge conflicts for zero benefit. Only apply Boy Scout cleanups to files we own (custom packages/folders).

When in doubt: ask "will this line cause a merge conflict the next time we pull upstream?" — if yes, find a less invasive approach.

## Fork Maintenance & Branching

This fork has a layered branch model (upstream → EST shared → per-customer) and strict rules about where custom code lives. **Branch naming, layering rules, commit/squash policy, `git rerere` setup, upstream version bumps, and conflict resolution recipes all live in [`.claude/docs/FORK_MAINTENANCE.md`](.claude/docs/FORK_MAINTENANCE.md).** Read it before creating branches, merging upstream, or cherry-picking.

The one load-bearing idea to internalize: **code-level isolation** (Upstream Compatibility rules 2 and 5) is what makes the branching model tractable — every custom line under `org.pih.warehouse.custom.*` / `src/js/custom/*` / `grails-app/migrations/custom/*` means "what's custom?" is answered by a file listing rather than a patch diff.

## Tracking Customizations

We do **not** maintain a separate `CUSTOMIZATIONS.md` patch list. Instead:

- **Every non-trivial custom feature goes through OpenSpec** (`openspec/changes/<change-name>/`) with a proposal, design, and tasks. On completion it's archived to `openspec/changes/archive/<change-name>/`.
- **The OpenSpec archive *is* the patch manifest.** To answer "what customizations exist on this fork?", list `openspec/changes/archive/`.
- **Every archived `design.md` must include an "Upstream touch points" section** listing modified upstream files (Upstream Compatibility rule 7) — that's the hitlist for future upstream merge conflicts.
- **Every archived change should note its "Deploy status"** — which customer branches it has been replayed onto, and, if submitted upstream, under what PR number.

Small config tweaks, seed data, and one-off fixes don't need archive entries.

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Grails 3.3.16 (Groovy 2.4.21) — runs on Spring Boot 1.5, but Grails owns DI, persistence, and the request lifecycle. Do **not** use Spring stereotypes (`@Autowired` / `@Service` / `@Repository`) on new beans. |
| **ORM** | GORM / Hibernate 5.2.18 |
| **Database** | MySQL 8 (MariaDB 10.3 also supported) |
| **Migrations** | Liquibase (via Grails Database Migration plugin) |
| **Frontend** | React 16.8 + Redux + Webpack 5 |
| **Build** | Gradle (backend), npm (frontend) |
| **Testing** | JUnit (backend), Jest + React Testing Library (frontend) |
| **Deployment** | Docker (OpenJDK 8 base), Docker Compose |
| **CI** | GitHub Actions |

## Project Structure

- `grails-app/` — Backend (controllers, services, domain classes, views, migrations)
- `src/js/` — React frontend source
- `src/main/` — Java source (helpers, utilities)
- `docker/` — Docker and Docker Compose configuration
- `openspec/` — Change proposals and design documents

## Domain Context

This is a **supply chain / logistics** application. When working on features, leverage the domain-specific skills installed in `.claude/skills/`:

- **inventory-demand-planning** — Forecasting, safety stock, reorder logic, ABC/XYZ classification
- **customs-trade-compliance** — Tariff classification, FTA handling, customs documentation
- **logistics-exception-management** — Shipment tracking, freight claims, carrier exception handling
- **returns-reverse-logistics** — Return processing, reverse supply chain flows
- **production-scheduling** — Manufacturing and production planning
- **quality-nonconformance** — Quality control, defect tracking, corrective actions

## Backend Conventions

OpenBoxes follows Grails conventions:

- **Controllers** are thin — parse input, delegate to services, return responses
- **Services** contain business logic and are transactional by default in Grails
- **Domain classes** define the data model with GORM mappings (not JPA annotations)
- **Database migrations** use Liquibase changesets in `grails-app/migrations/`. **Custom changesets go in `grails-app/migrations/custom/`**, aggregated by `custom/changelog.groovy` and included once from the master `changelog.groovy` — see `rules/custom-package-isolation.md`.

### Grails vs Pure Spring Boot

The Java/Spring rules in `.claude/rules/java/` apply with these Grails-specific adaptations:

- Use **GORM dynamic finders** and criteria queries instead of JPA `@Repository` interfaces
- Use **Grails service injection** (automatic by convention) instead of `@Autowired` / constructor injection
- Use **Groovy closures** and collection methods (`.collect`, `.findAll`, `.groupBy`) instead of Java Streams
- Domain class **constraints blocks** replace Bean Validation annotations
- **`grails-app/conf/`** holds config, not `application.yml` in `src/main/resources/`

## Frontend Conventions

- **Upstream** components live in `src/js/components/` — you can read/reference them freely but new custom components do **not** go here.
- **Custom** components, hooks, reducers, utilities go under `src/js/custom/<feature>/` (see `rules/custom-package-isolation.md`). The PreToolUse hook enforces this on new files.
- Redux actions/reducers follow the existing patterns in `src/js/reducers/` (for upstream) or `src/js/custom/<feature>/redux/` (for custom).
- API calls use Axios via existing API utilities (`src/js/utils/apiClient.js` and siblings).
- Styling uses SCSS (Bootstrap 4.6 base).

## Build & Run

```bash
# Backend
./gradlew bootRun

# Frontend (dev)
npm install && npm run watch

# Docker
cd docker && docker-compose up

# Tests
./gradlew test          # Backend
npm test                # Frontend
```

## Installed Claude Code Resources

### Agents (`.claude/agents/`)
- `java-reviewer` — Reviews Grails/Groovy + Java helper changes for upstream-isolation, GORM patterns, transactional-by-default services, Spock test quality, and Java 8 / Groovy 2.4 language floors. Use for every backend change.
- `java-build-resolver` — Fixes Gradle 4.10.3 / Grails 3.3.16 / Java 8 / Groovy 2.4 / Node 14 build errors with surgical edits. Knows the JDK-mismatch failure modes (`gradle-git-properties` reflective errors on JDK 11+) and the upstream-file edit policy.
- `react-reviewer` — Reviews React 16.8 / Redux / JSX changes under `src/js/**`. Enforces custom-package isolation, form-library consistency, and i18n.

### Skills (`.claude/skills/`)
- **Project-specific**: `code-review` (Grails layer paths + OpenBoxes gotchas)
- **Supply Chain Domain**: `customs-trade-compliance`, `inventory-demand-planning`, `logistics-exception-management`, `returns-reverse-logistics`, `production-scheduling`, `quality-nonconformance`

> **Intentionally excluded** (incompatible with this stack): `springboot-patterns`, `springboot-security`, `springboot-tdd`, `springboot-verification`, `jpa-patterns`, `java-coding-standards`. Those teach Spring Boot + JPA + Java 17+ patterns that contradict Grails / GORM / Java 8. Do **not** reintroduce them from upstream skill libraries.

### Rules (`.claude/rules/`)

Auto-loaded via `paths:` frontmatter — Claude sees them whenever it touches matching files.

- `custom-package-isolation.md` — **(CRITICAL)** all new code goes under `custom/` folders for upstream-merge safety. Auto-loads on `grails-app/**`, `src/main/**`, `src/main/groovy/**`, `src/main/java/**`, `src/main/webapp/**`, `src/test/**`, `src/integration-test/**`, `src/js/**`, `test/**`.
- `groovy/patterns.md` — Grails conventions, GORM query decision tree, transactional-by-default footgun, constraints/mappings, Liquibase DSL, functional Groovy idioms. Applies to `grails-app/**/*.groovy`, `src/main/groovy/**`.
- `groovy/testing.md` — Spock + Grails test mixins, integration tests, data-driven tables. Applies to `src/test/groovy/**`, `src/integration-test/groovy/**`, `test/**/*.groovy`.
- `java/patterns.md` / `java/coding-style.md` / `java/security.md` — for `src/main/java/**` helpers only. Java 8 floor, no JPA, no Spring stereotypes on new beans.
- `java/hooks.md` — manual compile commands for Java helpers (`./gradlew compileJava` / `compileGroovy`). Explicitly does **not** wire auto-format / static-analysis hooks — those would touch upstream files. Scoped to `src/main/java/**`, `src/test/java/**`.
- `web/coding-style.md` — Node 14 / React 16.8 / JSX / Redux / SCSS / Bootstrap 4.6 conventions. Applies to `src/js/**`.
- `web/patterns.md` — Component architecture, state management, API calls, forms (react-final-form + react-hook-form), tables, routing, i18n. Applies to `src/js/**`.
- `web/testing.md` — Jest 29 + @testing-library/react, Redux mock store, API mocking. Applies to `src/js/**`.
- `web/security.md` — XSS, CSRF, input validation, `apiClient` usage, `redux-persist` whitelisting. Applies to `src/js/**` and GSP views.
- `web/design-quality.md` — Admin-app UI quality rules (density, hierarchy, states, i18n, accessibility). Applies to `src/js/**` and GSP views.
- `web/performance.md` — Table virtualization, render count, bundle size, N+1 API calls. Applies to `src/js/**` and `webpack.config.js`.

### Commands (`.claude/commands/`)
- `/gradle-build` — Runbook for the OpenBoxes Grails Gradle build (JDK 8, Gradle 4.10.3). Lists the right task for each scenario (`bootRun`, `war`, `prepareDocker`, `test`, `integrationTest`) and hands deeper diagnosis off to the `java-build-resolver` agent.

> New features start with `/opsx:propose` (OpenSpec) — it generates the proposal, design, specs, and tasks. Custom-code placement is enforced by `rules/custom-package-isolation.md`, which auto-loads whenever Claude touches a matching file.

### Docs (`.claude/docs/`)
Team-facing operational docs about maintaining and working in this fork:
- `FORK_MAINTENANCE.md` — concrete git command recipes for maintaining the fork (version bumps, feature branches, conflict resolution, rollback, auditing what's custom)

### Context (`.claude/.context/`)
Per-developer local notes. **Not committed** — each developer maintains their own. Historical shared knowledge (barcode workflows, database schemas, API quirks, stock history, template guide, consumption fact aggregation, etc.) may still live here for reference on some machines but should not be assumed present.

## Relevant Agents for This Project

The team's shared agents are globally available; Claude picks by each agent's `description` field. For this Grails/Groovy + React codebase the most commonly relevant are:

- **java-reviewer** — Reviews Java/Groovy changes for layered architecture, GORM patterns, security, and concurrency. Use for every backend change. The project-specific `code-review` skill auto-supplements with OpenBoxes layer paths and Grails gotchas.
- **react-reviewer** — Reviews React/Redux/JSX changes under `src/js/**`. Enforces React 16.8 + Node 14 constraints, custom-package isolation, Redux patterns, form-library consistency, and i18n. Use for every frontend change.
- **java-build-resolver** — Fixes Gradle build errors, Groovy/Java compilation failures, and dependency resolution problems with minimal edits. Use when `./gradlew` fails.

Universal agents (`code-reviewer`, `backend-developer`, `security-reviewer`, `database-manager`, etc.) remain available for generic tasks.

## CI

GitHub Actions workflows live in `.github/workflows/`:

- `backend-tests.yml` — Gradle unit/integration tests
- `frontend-tests.yml` — Jest via `npm test`
- `dependecy-review.yml` — dependency-review action (upstream filename typo; **do not rename**)
- `docker-image.yml` — Docker image build
- `dbdocs.yml` — database documentation generation
- `do-github-release.yml` — release automation
- `on-change.yml`, `test-pull-request.yml`, `pull-request-formatter.yml`, `slack-notifier.yml` — utility/orchestration workflows

Keep `backend-tests.yml` and `frontend-tests.yml` green before opening a PR onto any `release/est/*` branch.

## Key Files

- `build.gradle` / `gradle.properties` / `settings.gradle` — backend build, version pins (`grailsVersion`, `gormVersion`, `owaspVersion`, etc.)
- `package.json` / `webpack.config.js` — frontend deps and bundling; note engines: Node 14, npm 6–7
- `grails-app/conf/application.yml`, `grails-app/conf/application.groovy` — Grails config; prefer external/env overrides, don't edit these for customizations
- `grails-app/conf/spring/resources.groovy` — Spring bean overrides (useful extension point)
- `grails-app/migrations/changelog.groovy` — Liquibase master changelog
- `docker/docker-compose.yml`, `docker/openboxes-config.properties` — dev container setup
- `openspec/` — customization proposals and archive (patch manifest)
- `.claude/docs/FORK_MAINTENANCE.md` — concrete git recipes for layered fork maintenance

## After Every Feature Change — Project-Specific

Beyond the team-wide checklist (README, PR description, OpenSpec specs):

1. **Liquibase registration** — if you added a migration, confirm it's included from `grails-app/migrations/changelog.groovy` (or a custom include) and has a unique id. Custom migration files belong under `grails-app/migrations/custom/` (or a clearly-prefixed folder) so they don't collide with upstream changesets.
2. **OpenSpec `design.md` upstream touch points** — the archived change must list every upstream file modified, plus a "Deploy status" line noting which customer branches it has been replayed onto (Upstream Compatibility rule 7).
3. **No committed bundles** — verify nothing under `grails-app/assets/javascripts/bundle*`, `grails-app/assets/stylesheets/bundle*`, or `src/main/webapp/webpack` was staged. `.gitignore` already excludes them, but check.
4. **i18n** — if you added UI strings, add matching keys in `grails-app/i18n/` messages properties so Crowdin (`crowdin.yml`) can pick them up.

## Pre-Commit Self-Review — Project-Specific

In addition to the team-wide checklist:

1. **Upstream-compatibility isolation.** New backend code under `org.pih.warehouse.custom.*`? New frontend under `src/js/custom/*`? New migrations under `grails-app/migrations/custom/*` (or clearly prefixed)? If you edited an upstream file, is the edit surgical and documented in the OpenSpec `design.md` touch-points section?
2. **Grails layering.** Controllers in `grails-app/controllers/` stay thin — no business logic. Services in `grails-app/services/` hold the logic. Domain classes use GORM (`static mapping`, `static constraints`); no `@Entity` / `@Column` / `@Repository`.
3. **Groovy idioms.** No Java-style `for` loops, `Stream`, or `Iterator` usage in `.groovy` files — use `.collect`, `.findAll`, `.groupBy`, `.find`, `.any`, `.every`, `.inject`. This is the Grails expression of the team-wide "prefer functional style" rule.
4. **Language floors.** OpenJDK 8 — no `var`, records, `List.of()`, or text blocks. Groovy 2.4 — no method references (`::`), `!in`, `!instanceof`. Node 14 — no optional chaining in Webpack / build scripts.
5. **No `@Autowired`.** Use Grails convention-based injection — declare dependencies as `def serviceName`.
6. **Raw SQL.** Prefer GORM dynamic finders, criteria queries, or HQL. If raw SQL is necessary, comment the reason inline.
7. **Boy Scout exemption for upstream files.** Do NOT clean up indentation, import order, or naming in upstream files you're editing — every incidental change becomes a future merge conflict. Apply the Boy Scout Rule only inside `org.pih.warehouse.custom.*`, `src/js/custom/*`, and `grails-app/migrations/custom/*`.

## Overrides to Team Conventions

- **Boy Scout Rule is suspended for upstream files.** See Upstream Compatibility rule 8. Apply cleanups only inside `org.pih.warehouse.custom.*`, `src/js/custom/*`, and `grails-app/migrations/custom/*`.

Note on functional style: the team-wide "prefer functional style" rule is **not** an override here — it matches the actual convention in upstream code (Groovy services use `.collect/.findAll/.groupBy/.inject` ~8.7× more often than imperative `for` loops; React components use `.map/.filter/.reduce` ~75× more often). Express the rule via Groovy collection methods in `.groovy` files (Streams are not idiomatic in Groovy 2.4) and via standard array methods in `.js`/`.jsx`.

## Gotchas

- **`.claude/.context/` is per-developer.** Already excluded via `.gitignore:53`. Do not commit anything under it. The rest of `.claude/` is tracked on this EST branch.
- **Upstream owns top-level `docs/`** (the admin guide). Never add team/fork docs there — use `.claude/docs/` to avoid merge collisions.
- **`dependecy-review.yml` misspelling** is upstream. Renaming it creates a forever merge conflict. Leave it.
- **Java 8 + Groovy 2.4 + Node 14** — the stack is frozen by upstream. Resist the urge to modernize syntax in files you touch.
