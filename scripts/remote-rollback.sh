#!/usr/bin/env bash
set -euo pipefail

RELEASES_ROOT="/opt/flatmate/releases"
CURRENT_LINK="/opt/flatmate/current"

if [[ ! -L "$CURRENT_LINK" ]]; then
  echo "No active release is available to roll back." >&2
  exit 1
fi

CURRENT_RELEASE="$(readlink -f "$CURRENT_LINK")"
PREVIOUS_RELEASE=$(find "$RELEASES_ROOT" -mindepth 1 -maxdepth 1 -type d -printf '%T@ %p\n' \
  | sort -nr \
  | cut -d' ' -f2- \
  | grep -Fvx "$CURRENT_RELEASE" \
  | head -n 1)

if [[ -z "$PREVIOUS_RELEASE" || ! -f "$PREVIOUS_RELEASE/docker-compose.yml" ]]; then
  echo "No previous release is available to roll back to." >&2
  exit 1
fi

(cd "$CURRENT_RELEASE" && docker compose down --remove-orphans)
ln -sfn "$PREVIOUS_RELEASE" "$CURRENT_LINK"
(cd "$PREVIOUS_RELEASE" && docker compose up -d)

for attempt in $(seq 1 30); do
  if curl -fsS http://127.0.0.1/health >/dev/null; then
    echo "Rollback completed: $PREVIOUS_RELEASE"
    exit 0
  fi
  sleep 5
done

echo "Previous release failed health validation; restoring the original release." >&2
(cd "$PREVIOUS_RELEASE" && docker compose down --remove-orphans) || true
ln -sfn "$CURRENT_RELEASE" "$CURRENT_LINK"
(cd "$CURRENT_RELEASE" && docker compose up -d)
exit 1
