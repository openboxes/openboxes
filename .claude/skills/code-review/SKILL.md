---
name: code-review
description: "Project-specific code review rules for OpenBoxes — Grails layer paths, architecture constraints, and known gotchas."
---

# Code Review — OpenBoxes

## Layer Diff Commands

- Domain: `git diff -- 'grails-app/domain/**'`
- Services: `git diff -- 'grails-app/services/**'`
- Controllers: `git diff -- 'grails-app/controllers/**'`
- Views (GSP): `git diff -- 'grails-app/views/**'`
- Migrations: `git diff -- 'grails-app/migrations/**'`
- Frontend (React): `git diff -- 'src/js/**'`
- Java Utils: `git diff -- 'src/main/java/**'`
- Config: `git diff -- 'grails-app/conf/**'`
- Build: `git diff -- 'build.gradle' 'package.json'`

## Architecture Rules to Enforce

- **Controllers must be thin.** Controllers in `grails-app/controllers/` should only parse input, delegate to services, and return responses. Flag any business logic (calculations, conditional workflows, direct domain queries beyond simple lookups) in controllers.
- **Services own business logic.** All non-trivial logic belongs in `grails-app/services/`. Services are transactional by default — explicit `@Transactional` should only appear when overriding the default (e.g., `@Transactional(readOnly = true)` or `@NotTransactional`).
- **No JPA annotations.** Domain classes use GORM mappings (`static mapping {}`, `static constraints {}`). Flag `@Entity`, `@Table`, `@Column`, `@Repository`, etc.
- **No `@Autowired` / constructor injection.** Grails uses convention-based injection. Declare service dependencies as properties (e.g., `def inventoryService`).
- **Use Groovy idioms.** Flag Java-style `for` loops, `Stream` usage, or `Iterator` patterns in Groovy files. Prefer `.collect`, `.findAll`, `.groupBy`, `.find`, `.any`, `.every`.
- **No raw SQL unless justified.** Data access should use GORM dynamic finders, criteria queries, or HQL. Raw SQL needs a comment explaining why.
- **Migration placement (CRITICAL — fork rule).** New Liquibase changesets go in `grails-app/migrations/custom/` (filename prefixed with date + feature, e.g. `2026-04-15-add-ims-fields.groovy`). They are aggregated by `grails-app/migrations/custom/changelog.groovy`, which is itself included from the master `grails-app/migrations/changelog.groovy` via a single `include file: 'custom/changelog.groovy'` line. **Never** edit existing upstream changesets and **never** drop new files into upstream version folders (`0.9.x/`, etc.) — that creates merge conflicts on every upstream pull. See `rules/custom-package-isolation.md`.

## Known Patterns and Gotchas

- **Java 8 constraint.** The project runs on OpenJDK 8. Flag any use of Java 11+ APIs (`List.of()`, `Map.of()`, `String.isBlank()`, `var`, records, sealed classes, text blocks).
- **Groovy 2.4 constraint.** No Groovy 3+ features (e.g., method references `::`, `!in`, `!instanceof`).
- **Node 14 constraint.** Frontend code must be compatible with Node 14. No optional chaining (`?.`) in build scripts (Webpack config, etc.).
- **Mixed rendering.** Some pages are GSP (server-rendered), others are React SPA. Changes to a page should match its existing rendering technology.
- **API controllers** in `grails-app/controllers/org/pih/warehouse/api/` typically extend `BaseDomainApiController` — check inheritance before adding duplicate functionality.
- **Commit messages.** Fork-internal commits use Conventional Commits (`feat(scope): …`, `fix(scope): …`) per the team-wide CLAUDE.md. Only commits being submitted upstream as PIH PRs use the upstream `OBPIH-XXXX description (#PR)` pattern — note that exception in the PR description.
- **Boolean API quirk.** The generic API may pass booleans as strings — review boolean handling in API controllers carefully.
- **Domain subpackages.** Domain classes are organized by subdomain: `core/`, `inventory/`, `order/`, `product/`, `shipping/`, `requisition/`, etc. New domain classes should go in the appropriate subpackage.
- **Service subpackages.** Mirror the domain package structure: `api/`, `core/`, `inventory/`, `order/`, etc.
