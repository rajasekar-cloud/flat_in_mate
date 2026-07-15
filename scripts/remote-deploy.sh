#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 7 ]]; then
  echo "Usage: remote-deploy.sh <region> <table> <media-bucket> <runtime-parameter> <allowed-origins> <log-group> <release-directory>" >&2
  exit 2
fi

AWS_REGION_VALUE="$1"
DYNAMODB_TABLE_NAME_VALUE="$2"
S3_BUCKET_VALUE="$3"
RUNTIME_PARAMETER_NAME="$4"
ALLOWED_ORIGINS_VALUE="$5"
CLOUDWATCH_LOG_GROUP_VALUE="$6"
RELEASE_DIRECTORY="$7"
RELEASES_ROOT="/opt/flatmate/releases"
CURRENT_LINK="/opt/flatmate/current"

case "$RELEASE_DIRECTORY" in
  "$RELEASES_ROOT"/*) ;;
  *)
    echo "Release directory must remain under $RELEASES_ROOT" >&2
    exit 2
    ;;
esac

if [[ ! -f "$RELEASE_DIRECTORY/docker-compose.yml" ]] || ! compgen -G "$RELEASE_DIRECTORY/target/*.jar" >/dev/null; then
  echo "Deployment artifact is incomplete." >&2
  exit 1
fi

umask 077
RUNTIME_ENV_FILE="$(mktemp /tmp/flatmate-runtime-env.XXXXXX)"
trap 'rm -f "$RUNTIME_ENV_FILE"' EXIT

aws ssm get-parameter \
  --name "$RUNTIME_PARAMETER_NAME" \
  --with-decryption \
  --region "$AWS_REGION_VALUE" \
  --query 'Parameter.Value' \
  --output text > "$RUNTIME_ENV_FILE"

if ! grep -q '^JWT_SECRET=.' "$RUNTIME_ENV_FILE"; then
  echo "The runtime SecureString must contain a non-empty JWT_SECRET entry." >&2
  exit 1
fi

if grep -Eq '^(AWS_ACCESS_KEY_ID|AWS_SECRET_ACCESS_KEY|AWS_SESSION_TOKEN)=' "$RUNTIME_ENV_FILE"; then
  echo "Static AWS credentials are forbidden in the runtime parameter." >&2
  exit 1
fi

sed 's/\r$//' "$RUNTIME_ENV_FILE" > "$RELEASE_DIRECTORY/.env"
cat >> "$RELEASE_DIRECTORY/.env" <<EOF
AWS_REGION=$AWS_REGION_VALUE
DYNAMODB_TABLE_NAME=$DYNAMODB_TABLE_NAME_VALUE
S3_BUCKET=$S3_BUCKET_VALUE
ALLOWED_ORIGINS=$ALLOWED_ORIGINS_VALUE
CLOUDWATCH_LOG_GROUP=$CLOUDWATCH_LOG_GROUP_VALUE
EOF
chmod 0600 "$RELEASE_DIRECTORY/.env"

PREVIOUS_RELEASE=""
if [[ -L "$CURRENT_LINK" ]]; then
  PREVIOUS_RELEASE="$(readlink -f "$CURRENT_LINK")"
fi

cd "$RELEASE_DIRECTORY"
docker compose build --pull

if [[ -n "$PREVIOUS_RELEASE" && -f "$PREVIOUS_RELEASE/docker-compose.yml" ]]; then
  (cd "$PREVIOUS_RELEASE" && docker compose down --remove-orphans) || true
fi

ln -sfn "$RELEASE_DIRECTORY" "$CURRENT_LINK"
docker compose up -d

HEALTHY=false
for attempt in $(seq 1 30); do
  if curl -fsS http://127.0.0.1/health >/dev/null; then
    HEALTHY=true
    break
  fi
  echo "Waiting for local health check ($attempt/30)..."
  sleep 5
done

if [[ "$HEALTHY" != "true" ]]; then
  echo "New release failed its local health check; rolling back." >&2
  docker compose logs --tail 100 backend >&2 || true
  docker compose down --remove-orphans || true

  if [[ -n "$PREVIOUS_RELEASE" && -f "$PREVIOUS_RELEASE/docker-compose.yml" ]]; then
    ln -sfn "$PREVIOUS_RELEASE" "$CURRENT_LINK"
    (cd "$PREVIOUS_RELEASE" && docker compose up -d)
  fi
  exit 1
fi

docker image prune -f >/dev/null

find "$RELEASES_ROOT" -mindepth 1 -maxdepth 1 -type d -printf '%T@ %p\n' \
  | sort -nr \
  | tail -n +4 \
  | cut -d' ' -f2- \
  | while IFS= read -r old_release; do
      case "$old_release" in
        "$RELEASES_ROOT"/*) rm -rf -- "$old_release" ;;
      esac
    done

echo "Deployment completed and passed the local health check."
