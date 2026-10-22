#!/usr/bin/env bash
# Deploy one PR preview behind Traefik on the VPS. Invoked over SSH by the preview workflow.
# Usage: preview-deploy.sh <project> <host> <image>
#   project  compose project name, e.g. pr-123
#   host     preview hostname, e.g. pr-123.preview.openboxes.com
#   image    image reference to run (the per-PR ttl.sh image)
# Env: PREVIEW_ACME_EMAIL (required), PREVIEW_MAX_CONCURRENT (optional, default 3)
set -euo pipefail

PROJECT="$1"
HOST="$2"
IMAGE="$3"
DIR="$HOME/openboxes-preview"
MAX="${PREVIEW_MAX_CONCURRENT:-3}"
: "${PREVIEW_ACME_EMAIL:?PREVIEW_ACME_EMAIL must be set}"

# Ensure the shared proxy network and Traefik are running (idempotent).
docker network create traefik 2>/dev/null || true
PREVIEW_ACME_EMAIL="$PREVIEW_ACME_EMAIL" \
  docker compose -p preview-infra -f "$DIR/docker-compose.traefik.yml" up -d

# Concurrency guard (RAM protection): count distinct running pr-* projects.
mapfile -t projects < <(docker ps --format '{{.Label "com.docker.compose.project"}}' | grep '^pr-' | sort -u)
count="${#projects[@]}"
already=0
for p in "${projects[@]}"; do [ "$p" = "$PROJECT" ] && already=1; done
if [ "$already" -eq 0 ] && [ "$count" -ge "$MAX" ]; then
  echo "Cap reached: ${count} preview(s) running (PREVIEW_MAX_CONCURRENT=${MAX}). Close a PR to free a slot." >&2
  exit 1
fi

OB_IMAGE="$IMAGE" PREVIEW_PROJECT="$PROJECT" PREVIEW_HOST="$HOST" \
  docker compose -p "$PROJECT" -f "$DIR/docker-compose.preview.yml" up -d --pull always --remove-orphans

# Reclaim disk from superseded/torn-down preview images (each build is a fresh ~1GB image),
# so the VPS doesn't fill up. Images backing running containers are in use and kept.
docker image prune -af >/dev/null 2>&1 || true

echo "Deployed ${PROJECT} -> https://${HOST}/openboxes"
