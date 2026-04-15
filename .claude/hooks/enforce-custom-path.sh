#!/usr/bin/env bash
# PreToolUse hook — enforces the custom-path isolation rule for new files.
#
# Fires on Write only (not Edit). Blocks the write when a NEW source file
# is being created outside a custom/ folder. Existing upstream files (already
# tracked by git) are always allowed through because Write may be used to
# overwrite them.
#
# See .claude/rules/custom-package-isolation.md for the full rule.
#
# Exit codes:
#   0  — allow (not a matching path, or under custom/, or already tracked)
#   2  — block (stderr message is shown to the model)

set -euo pipefail

# Read the tool input JSON from stdin.
input="$(cat)"

# Extract the target path. Without jq we do a minimal grep; jq is preferred.
if command -v jq >/dev/null 2>&1; then
    file_path="$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')"
else
    file_path="$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
fi

if [ -z "${file_path:-}" ]; then
    exit 0
fi

# Normalize to a repo-relative path.
# CLAUDE_PROJECT_DIR is set by the harness; fall back to PWD.
repo_root="${CLAUDE_PROJECT_DIR:-$PWD}"
case "$file_path" in
    /*) rel="${file_path#$repo_root/}" ;;
    *)  rel="$file_path" ;;
esac

# Only enforce inside the tracked source trees, and only on source-like
# extensions. Uses bash regex so arbitrary nesting depth works.
if [[ "$rel" =~ ^grails-app/migrations/.*\.groovy$ ]]; then
    migration_file=1
elif [[ "$rel" =~ ^grails-app/.*\.(groovy|java)$ ]]; then
    migration_file=0
elif [[ "$rel" =~ ^src/main/(groovy|java)/.*\.(groovy|java)$ ]]; then
    migration_file=0
elif [[ "$rel" =~ ^src/js/.*\.(js|jsx|scss)$ ]]; then
    migration_file=0
else
    # Not a source file we care about — allow.
    exit 0
fi

# If the file is already tracked by git, this is an edit to an existing
# upstream file — allowed (Edit would be preferred, but we don't block Write
# on tracked files because overwrites are sometimes legitimate).
if git -C "$repo_root" ls-files --error-unmatch -- "$rel" >/dev/null 2>&1; then
    exit 0
fi

# From here on, this is a NEW file.

# Migrations: must live under grails-app/migrations/custom/
if [ "${migration_file:-0}" = "1" ]; then
    case "$rel" in
        grails-app/migrations/custom/*) exit 0 ;;
    esac
    cat >&2 <<EOF
BLOCKED by .claude/hooks/enforce-custom-path.sh:
  New Liquibase migration must live under grails-app/migrations/custom/.
  Rejected path: $rel
  Expected:      grails-app/migrations/custom/$(date +%Y-%m-%d)-<feature>.groovy
  See .claude/rules/custom-package-isolation.md (custom-package isolation rule)
  and wire it into grails-app/migrations/custom/changelog.groovy.
EOF
    exit 2
fi

# All other source files: path must contain a /custom/ segment
# (equivalent to org.pih.warehouse.custom.* for Java/Groovy package trees).
case "/$rel/" in
    */custom/*) exit 0 ;;
esac

# Block.
cat >&2 <<EOF
BLOCKED by .claude/hooks/enforce-custom-path.sh:
  New source files must live under a custom/ path so upstream merges stay clean.
  Rejected path: $rel

  Re-route to one of:
    grails-app/{services,controllers,domain,taglib,jobs}/org/pih/warehouse/custom/<feature>/...
    src/main/groovy/org/pih/warehouse/custom/<feature>/...
    src/main/java/org/pih/warehouse/custom/<feature>/...
    src/js/custom/<feature>/{components,hooks,utils,redux,__tests__}/...

  See .claude/rules/custom-package-isolation.md for the full layer -> path table.
  (This hook only fires on Write; editing an existing upstream file via Edit is allowed.)
EOF
exit 2
