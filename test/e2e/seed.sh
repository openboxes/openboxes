#!/usr/bin/env bash
# Seed the running e2e stack: test/e2e/fixtures/seed.sql (reference data no API creates), then the
# Playwright `setup` project (signs in, creates the seeded depot through OpenBoxes' Location API,
# saves the browser sign-in state). Idempotent. usage: test/e2e/seed.sh
set -euo pipefail
E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "$E2E_DIR/lib/common.sh"
# the mariadb image ships `mariadb`, the mysql image `mysql`
# shellcheck disable=SC2016 # $@ is expanded by the container's shell
compose_running exec -T db sh -c 'if command -v mariadb >/dev/null; then exec mariadb "$@"; else exec mysql "$@"; fi' sh \
  -uopenboxes -popenboxes openboxes < "$E2E_DIR/fixtures/seed.sql"
log "applied fixtures/seed.sql"
node_env
# the profile's reports directory: two stacks seeding at once must not share Playwright's output dir
(cd "$E2E_DIR" && E2E_REPORTS_DIR="${E2E_REPORTS_DIR:-$E2E_REPORTS}" npx playwright test --project=setup --reporter=line >&2)
log "seeded"
