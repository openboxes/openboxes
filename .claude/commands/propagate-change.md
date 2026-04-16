---
description: Make a change on a parent branch and propagate it down through the branch hierarchy (EST → client → feature). Handles git archaeology, level selection, merges, and push confirmation.
---

# /propagate-change — Propagate a change through the branch hierarchy

You are about to help the user make a change on a parent branch and merge it down through the fork's layered branch model. This is used for rule updates, CLAUDE.md changes, config overrides, docs — anything that should live higher than the current feature branch.

## Step 1: Understand the change

Ask the user (if not already provided via $ARGUMENTS):
- **What change do you need to make?** (e.g., "add a camelCase naming rule to custom-package-isolation.md")
- **Which file(s) will be modified or created?**

If the user already described the change in $ARGUMENTS, confirm your understanding before proceeding.

## Step 2: Map the branch hierarchy

Run these commands to discover the branch structure:

```bash
# Current branch
git branch --show-current

# All remote EST/client branches
git branch -r | grep -E 'release/est/'

# Find the merge base between the current branch and each release branch
# to determine the ancestry chain
```

Build the hierarchy by tracing merge bases. The typical structure is:

```
develop (upstream)
  └─ release/est/<version>           ← EST shared layer
       └─ release/est/<client>/<version>  ← per-client layer (one or more)
            └─ feature/* or fix/*         ← current working branch
```

Present the discovered hierarchy to the user as a tree, including commit hashes so they can verify.

## Step 3: Ask the propagation level

Ask the user:

> **Up to what level should this change be committed?**
>
> 1. **EST shared** (`release/est/<version>`) — applies to all clients
> 2. **Client** (`release/est/<client>/<version>`) — just this client
> 3. **Current branch only** — no propagation needed
>
> The change will be committed at the chosen level and merged down to your current branch.

Wait for their answer before proceeding.

## Step 4: Pre-flight checks

```bash
# Ensure working tree is clean
git status --short

# Ensure the target branch is up to date with remote
git fetch origin
git log --oneline origin/<target-branch>..<target-branch>  # should be empty
git log --oneline <target-branch>..origin/<target-branch>  # should be empty
```

If the working tree is dirty, ask the user to commit or stash first.
If the target branch is behind remote, fast-forward it first.
If the target branch is ahead of remote, warn the user and ask how to proceed.

## Step 5: Make the change

1. **Record the current branch** so you can return to it later.
2. **Checkout the target branch** (the level chosen in Step 3).
3. **Apply the change** — edit/create the file(s) as described.
4. **Commit** using conventional commits (`docs(rules):`, `chore(config):`, etc.).
5. Do NOT push yet.

## Step 6: Merge down through the hierarchy

**Always merge, never cherry-pick.** Cherry-picking creates duplicate commits with different SHAs, which causes merge conflicts when branches merge naturally later. Merging preserves commit lineage so git knows the change is already present on child branches.

For each level, merge the parent into the child:

```bash
git checkout <next-level-branch>
git merge <parent-branch> --no-edit
```

If there are conflicts:
- Show the conflicts to the user
- Ask how to resolve them
- Do NOT force-resolve or skip

Continue until you reach the original working branch.

## Step 7: Verify

```bash
# Show the full log from the target branch down to current
git log --oneline --graph <target-branch>..HEAD

# Confirm the change is present
# (read the modified file to verify)
```

Show the user the merge chain and the final state of the changed file(s).

## Step 8: Push

Ask the user which branches to push. List each branch that has unpushed commits:

```bash
git log --oneline origin/<branch>..<branch>
```

**Never push without explicit user confirmation.** Present the list and wait for approval:

> The following branches have unpushed commits:
> - `release/est/<version>` — N commit(s)
> - `release/est/<client>/<version>` — N commit(s)
> - `feature/<name>` — N commit(s)
>
> Which branches should I push? (all / list specific / none)

Push only what the user approves. Never force push.

## Important constraints

- **Always merge, never cherry-pick** — preserves commit lineage and avoids duplicate-commit conflicts.
- **Never force push** to any branch.
- **Never amend** commits that are already on remote.
- **Always use conventional commits** for the change itself.
- **Always return to the original branch** when done.
- **If anything goes wrong**, stop and explain — don't try to recover silently.
