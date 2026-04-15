# Fork Maintenance — Command Recipes

Concrete git command walkthroughs for maintaining the OpenBoxes fork. **The rules and policy live in `CLAUDE.md` (Fork Maintenance & Branching section)** — this doc is just the "how do I actually run the commands" companion. Read the policy first; reach for this when you need a recipe.

Branch naming reminder:
- `develop` — upstream-only mirror. Never a source of our customer work.
- `release/<version>` — upstream release mirrors (e.g. `release/0.9.6-hotfix1`, `release/0.9.8`). Upstream-pristine.
- `release/<client>/<version>` — per-client customization branch (e.g. `release/spocc/0.9.6-hotfix1`). Built on top of the matching upstream release.
- `feature/<client>-<name>` / `fix/<client>-<name>` — short-lived, branch from and merge back to `release/<client>/<version>`.

---

## Recipe 0 — One-time setup: `git rerere`

Enable globally so recurring merge conflicts resolve themselves on repeated replays:

```bash
git config --global rerere.enabled true
```

Do this once per developer machine. You never have to think about it again.

---

## Recipe 1 — Create a new customer customization branch

Use this when you're standing up a new customer for the first time (e.g. adding SPOCC, or later adding Tajikistan).

```bash
git fetch origin
git checkout release/0.9.6-hotfix1          # or whichever upstream baseline
git pull
git checkout -b release/spocc/0.9.6-hotfix1
git push -u origin release/spocc/0.9.6-hotfix1
```

For a second customer:

```bash
git checkout release/0.9.6-hotfix1           # same upstream baseline (or different, up to you)
git checkout -b release/tjk-lmis/0.9.6-hotfix1
git push -u origin release/tjk-lmis/0.9.6-hotfix1
```

Two independent customization branches. No shared customer code between them.

---

## Recipe 2 — Start a feature branch for customer work

```bash
git checkout release/spocc/0.9.6-hotfix1
git pull
git checkout -b feature/spocc-season-entity
# ...do the work, commit
git push -u origin feature/spocc-season-entity
# open a PR targeting release/spocc/0.9.6-hotfix1 (NOT develop, NOT main)
```

**Target the right branch when opening the PR.** GitHub defaults to the repo's default branch, which is usually `develop`. Change it to `release/spocc/0.9.6-hotfix1` explicitly. Getting this wrong is the #1 way customer code leaks into `develop`.

When the PR merges:
- Squashing is allowed on `release/<client>/<version>`, not on `develop`. See the squashing section in `CLAUDE.md`.
- Preserve any `(cherry picked from commit X)` trailers in the squash commit body.

---

## Recipe 3 — Upstream version bump via `cherry-pick` (recommended)

Scenario: upstream just tagged `release/0.9.8`. Brad's instance is running off `release/spocc/0.9.6-hotfix1`. You want to move it to 0.9.8.

```bash
# 1. Fetch the new upstream release
git fetch origin
git checkout release/0.9.8             # upstream-pristine mirror. Create from upstream if missing.
git pull

# 2. Create the new custom branch from the new upstream baseline
git checkout -b release/spocc/0.9.8

# 3. Replay the custom layer onto it.
#    The A..B range means "commits reachable from B but not from A" —
#    i.e. exactly your custom commits, nothing from the old upstream.
git cherry-pick release/0.9.6-hotfix1..release/spocc/0.9.6-hotfix1

# 4. Conflicts will surface — see Recipe 5 for the playbook.
#    For each conflict:
#      edit file → git add <file> → git cherry-pick --continue
#    To skip a commit that upstream has already absorbed:
#      git cherry-pick --skip
#    To bail out entirely and try a different approach:
#      git cherry-pick --abort

# 5. Build + test
./gradlew clean build
./gradlew test
npm test
cd docker && docker-compose up -d
# manually smoke-test the IMS workflows

# 6. Push
git checkout release/spocc/0.9.8
git push -u origin release/spocc/0.9.8

# 7. Deploy: staging first, bake, then cut prod over.
#    Leave release/spocc/0.9.6-hotfix1 alone until 0.9.8 has proven itself in prod.
#    It is your rollback branch.
```

### What the `A..B` range actually picks up

```
upstream 0.9.6-hotfix1  ●━●━●━●                              ← release/0.9.6-hotfix1
                                ╲
                                 ●━●━●━●                     ← release/spocc/0.9.6-hotfix1
                                 ^^^^^^^ these 4 commits are A..B

upstream 0.9.8  ●━●━●━●━●━●━●                                ← release/0.9.8
                              ╲
                               ●━●━●━●                       ← release/spocc/0.9.8 (after replay)
                               the same 4 custom commits, replayed on the new baseline
```

---

## Recipe 4 — Upstream version bump via `rebase --onto` (alternative)

Same result as Recipe 3, different command shape. Pick whichever reads more naturally. Both trigger `rerere` identically.

```bash
git fetch origin
git checkout -b release/spocc/0.9.8 release/spocc/0.9.6-hotfix1
git rebase --onto release/0.9.8 release/0.9.6-hotfix1
```

Read as: "Take the commits on the current branch that aren't in `release/0.9.6-hotfix1`, and replay them onto `release/0.9.8`." Same custom-layer replay, expressed as a rebase instead of a range cherry-pick.

**Heads up:** rebase rewrites commit hashes. If the old branch had commits shared with another branch (e.g. a feature branch not yet merged), coordinate before rebasing. Cherry-pick (Recipe 3) doesn't have this concern since it creates fresh commits on a new branch without touching the old one.

---

## Recipe 5 — Conflict resolution playbook

Conflicts during a version bump come from two places, roughly in order of likelihood:

### 5a. Upstream touched files your customization also touched

This is exactly what Upstream Compatibility rule 7 exists for. Each OpenSpec archived change must list its upstream touch points in `design.md`. Before you start the cherry-pick, grep for them:

```bash
grep -r "Upstream touch points" openspec/changes/archive/
```

That gives you the hotspot list. Eyeball each file to see whether upstream changed it between 0.9.6-hotfix1 and 0.9.8:

```bash
git log release/0.9.6-hotfix1..release/0.9.8 -- path/to/touched/file
```

If upstream touched it, expect a conflict on that file during the replay. If they didn't, it'll replay clean.

### 5b. Liquibase changelog include order

If upstream added new migrations to `grails-app/migrations/changelog.groovy`, your custom `include` directive may end up in a different relative position. The fix is always the same: **keep the custom include *at the end*** of `changelog.groovy`. During conflict resolution, accept upstream's new include lines and move your custom include below them.

Per Upstream Compatibility rule 5, your migrations are additive and live in their own file/folder, so the actual changeset contents never conflict — only the registration order.

### 5c. `custom/*` code conflicting

If a file under `org.pih.warehouse.custom.*` or `src/js/custom/*` shows a conflict, **stop and investigate**. Code-level isolation means these files shouldn't exist upstream and therefore can't conflict. If one does, either:
- Someone accidentally committed customer code outside a `custom/*` path (audit the prior PR), or
- You're on the wrong base branch (check `git log --oneline -5`), or
- An upstream merge to your custom branch re-introduced an upstream path that collides with your custom path by coincidence (rare).

Investigate before resolving.

### 5d. Generic conflict resolution commands

```bash
# See what's conflicted
git status

# After editing a file to resolve:
git add <file>

# Continue the cherry-pick / rebase:
git cherry-pick --continue    # or git rebase --continue

# Skip the current commit (upstream already has equivalent changes):
git cherry-pick --skip        # or git rebase --skip

# Abort everything and go back to where you were:
git cherry-pick --abort       # or git rebase --abort
```

`git rerere` (Recipe 0) silently records every conflict resolution and auto-applies it on the next replay, which matters when you do a version bump twice (e.g. staging first, prod later, or a retry after a build failure).

---

## Recipe 6 — Rollback after a bad version bump

You cut `release/spocc/0.9.8` over to prod. It broke. You need to get Brad back on 0.9.6-hotfix1.

```bash
# On the deployment side: redeploy from the old branch
git checkout release/spocc/0.9.6-hotfix1
# rebuild + redeploy the Docker image
cd docker && docker-compose build && docker-compose up -d

# On the repo side: leave the old branch alone. Investigate what broke on 0.9.8,
# fix it on release/spocc/0.9.8 (or start over with a new branch if rebase is cleaner),
# and try again.
```

**Do not delete `release/spocc/0.9.6-hotfix1` until the new version has baked in prod for a meaningful period.** Days at minimum, a full gift-box season ideally. Old branches are cheap; lost prod data is expensive.

If you want to mark the old tip as "this was prod at time T" before eventually deleting, tag it:

```bash
git tag release/spocc/0.9.6-hotfix1-pre-0.9.8-cutover release/spocc/0.9.6-hotfix1
git push origin release/spocc/0.9.6-hotfix1-pre-0.9.8-cutover
```

Tags are lightweight and survive branch deletion.

---

## Recipe 7 — Auditing "what's custom on this instance?"

Two methods; run both and cross-check.

### 7a. By git diff (the mechanical truth)

```bash
git diff release/0.9.6-hotfix1..release/spocc/0.9.6-hotfix1 --stat
```

That's the literal list of files changed by your custom layer. `--stat` gives you the summary; drop it for the full diff.

### 7b. By OpenSpec archive (the narrative truth)

```bash
ls openspec/changes/archive/
```

Each archived change is one customer-facing feature. Cross-reference the two — anything in the git diff that isn't represented by an archived change is either a trivial commit that didn't warrant a proposal (seed data, config tweaks) or a process leak to investigate.

---

## Quick reference

| I want to... | Recipe |
|---|---|
| Enable `git rerere` | 0 |
| Create `release/spocc/0.9.6-hotfix1` for the first time | 1 |
| Add `release/tjk-lmis/0.9.6-hotfix1` as a second customer | 1 |
| Start a new feature branch for SPOCC | 2 |
| Bump SPOCC from 0.9.6 to 0.9.8 via cherry-pick | 3 |
| Bump via `rebase --onto` instead | 4 |
| Resolve conflicts during a bump | 5 |
| Roll prod back to the previous baseline | 6 |
| Audit what's custom on an instance | 7 |
