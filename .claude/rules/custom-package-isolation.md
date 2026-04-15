---
paths:
  - "grails-app/**"
  - "src/main/**"
  - "src/main/groovy/**"
  - "src/main/java/**"
  - "src/main/webapp/**"
  - "src/test/**"
  - "src/integration-test/**"
  - "src/js/**"
  - "test/**"
---

# Custom Package Isolation (CRITICAL)

> This is the **single most important rule** in this fork. Every new custom line of code MUST live under a `custom/` subfolder or `org.pih.warehouse.custom.*` package. Failure to do this creates merge conflicts forever when we pull upstream updates, because any file we edited becomes a conflict point when upstream also edits it.

## The Rule

**All new code goes under a `custom` path.** Period.

| Layer | Where new files go |
|---|---|
| Grails domain classes | `grails-app/domain/org/pih/warehouse/custom/<feature>/` |
| Grails services | `grails-app/services/org/pih/warehouse/custom/<feature>/` |
| Grails controllers | `grails-app/controllers/org/pih/warehouse/custom/<feature>/` |
| Grails views (GSP) | `grails-app/views/custom/<feature>/` |
| Grails taglibs | `grails-app/taglib/org/pih/warehouse/custom/<feature>/` |
| Grails interceptors | `grails-app/controllers/org/pih/warehouse/custom/<feature>/` |
| Grails commands / jobs | `grails-app/jobs/org/pih/warehouse/custom/<feature>/` |
| Liquibase migrations | `grails-app/migrations/custom/` (filename prefixed with date + feature) |
| i18n messages | `grails-app/i18n/custom/<feature>-messages_<locale>.properties` (if separated from upstream) |
| Java helpers | `src/main/java/org/pih/warehouse/custom/<feature>/` |
| Groovy helpers | `src/main/groovy/org/pih/warehouse/custom/<feature>/` |
| React components | `src/js/custom/<feature>/components/` |
| React hooks | `src/js/custom/<feature>/hooks/` |
| React utils | `src/js/custom/<feature>/utils/` |
| Redux actions / reducers / selectors | `src/js/custom/<feature>/redux/` |
| Frontend tests | `src/js/custom/<feature>/__tests__/` |
| Backend unit tests | `src/test/groovy/org/pih/warehouse/custom/<feature>/` |
| Backend integration tests | `src/integration-test/groovy/org/pih/warehouse/custom/<feature>/` |

**`<feature>` is a short kebab-case or camelCase name** describing the feature: `gs1-barcode`, `ims-migration`, `seasonCartonCascade`, etc. Match the convention already in use within the custom folder.

## Why

OpenBoxes is a **fork** of upstream PIH OpenBoxes. We pull upstream updates periodically — every merge replays upstream commits onto our EST layer, and onto every customer branch on top of EST. The number of merge conflicts is proportional to the number of upstream files we've touched. If every custom line lives in a `custom/` folder that upstream never touches, we get **zero conflicts** on those files.

**The corollary:** the Boy Scout Rule from the global CLAUDE.md is **suspended** for any file outside the custom folders. Do not reformat, reorder imports, rename symbols, or "clean up" upstream files. Every incidental edit is a future merge conflict for zero functional benefit.

## When You MUST Touch an Upstream File

Sometimes a feature genuinely requires modifying an existing upstream file (adding a menu link, registering a new controller, hooking into an existing wizard step). The rules are:

1. **Keep the edit surgical and localized.** Change only the lines the feature requires. No incidental cleanup, no import reordering, no rename-while-you're-there.
2. **Prefer extension points** — Grails service injection, event listeners, taglib additions, React component composition via props/children — over rewriting the upstream logic.
3. **Document the touch point.** Every OpenSpec change (`openspec/changes/<change>/design.md`) MUST include an "Upstream touch points" section listing the upstream files modified and the one-line reason for each. This is our merge-conflict hitlist for future upstream pulls.
4. **Prefer a new file + a one-line include** over editing a big file. Example: instead of inlining a new section into `grails-app/views/dashboard/index.gsp`, create `grails-app/views/custom/<feature>/_section.gsp` and add a single `<g:render template="/custom/<feature>/section"/>` line to `index.gsp`.

## When You're Creating a New File

**Before you write the first line, check the destination path.** If the path does not contain `custom/` or `org.pih.warehouse.custom.*`, STOP and re-route.

### Backend example

```
❌ grails-app/services/org/pih/warehouse/inventory/IMSImportService.groovy
✅ grails-app/services/org/pih/warehouse/custom/imsImport/IMSImportService.groovy
```

### Frontend example

```
❌ src/js/components/imsImport/ImsImportPage.jsx
✅ src/js/custom/imsImport/components/ImsImportPage.jsx
```

### Migration example

```
❌ grails-app/migrations/0.9.x/add-ims-fields.groovy
✅ grails-app/migrations/custom/2026-04-13-add-ims-fields.groovy
```

## Wiring It Up

### Liquibase

Add the custom changeset to the master changelog **via an include line**, not by inlining:

```groovy
// grails-app/migrations/changelog.groovy
databaseChangeLog = {
    // ... upstream includes unchanged ...
    include file: 'custom/changelog.groovy'  // <-- add this ONE line
}
```

Then `grails-app/migrations/custom/changelog.groovy` aggregates all custom migrations:

```groovy
databaseChangeLog = {
    include file: '2026-04-13-add-ims-fields.groovy'
    include file: '2026-04-15-ims-indexes.groovy'
}
```

This minimizes upstream-file touches to one line.

### Spring beans

If a custom class needs to be registered as a Spring bean (rare; Grails auto-scans `grails-app/` subfolders), add it in `grails-app/conf/spring/resources.groovy`. That file is edited rarely by upstream, so diffs there have low conflict risk.

### React routes

If you're adding a new React route, the route table is in `src/js/routes/` (or wherever the project has it). That's an upstream file — the edit is a single `<Route>` line, which is acceptable and should be noted in the design.md touch points.

## Boy Scout Rule

The team-wide Boy Scout Rule from `~/.claude/est/CLAUDE.md` is **suspended inside any upstream file** (anything not under `custom/` or `org.pih.warehouse.custom.*`). Apply cleanups ONLY inside the custom folders.

## Enforcement in Code Review

The `code-review`, `java-reviewer`, and `react-reviewer` agents all check this rule. Any new file outside the custom folders is flagged as **CRITICAL** — a blocker, not a warning.

## Starting a New Feature

Start every non-trivial custom feature with `/opsx:propose` — it generates the OpenSpec change folder (`openspec/changes/<change>/`) with `proposal.md`, `design.md`, `tasks.md`, and the spec files. The `design.md` template must include an "Upstream touch points" section if the work modifies any upstream file.

When you create the first real backend or frontend file for the feature, place it under the matching `custom/` path from the table above. There is no directory scaffolder — this rule file plus the reviewer agents are the enforcement.

## FAQ

**Q: What if the feature is tiny — one controller and one GSP? Do I still need the whole structure?**
A: Yes. The path `grails-app/controllers/org/pih/warehouse/custom/<feature>/FooController.groovy` is not overhead — it's the difference between "upstream merge just works" and "upstream merge has conflicts".

**Q: The existing OpenBoxes code doesn't follow this structure. Can I put my new file next to the existing code for consistency?**
A: No. The existing code is upstream-pristine; our custom additions live in a parallel `custom/` structure. Consistency with upstream means **leaving upstream alone**, not adopting its layout for new files.

**Q: What about tests for upstream code I had to edit?**
A: Write the tests under `src/test/groovy/org/pih/warehouse/custom/<feature>/` even if the code under test is upstream. The test file is yours, so it lives in your tree.

**Q: What if two customers share a custom feature?**
A: Then it belongs on the EST shared layer (`release/est/<version>`), not the per-customer branch. The file layout is the same — `org.pih.warehouse.custom.<feature>/` — it's just pushed down the stack one level.
