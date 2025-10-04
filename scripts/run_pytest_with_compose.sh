#!/bin/bash
set -euo pipefail

COMPOSE_FILE=${COMPOSE_FILE:-docker-compose.ci.yml}
SERVICES=(${SERVICES:-backend1 backend2})
CMD=("$@")

if [ ${#CMD[@]} -eq 0 ]; then
    echo "Usage: $0 <pytest arguments>" >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "docker command not found" >&2
    exit 1
fi

cleanup() {
    docker compose -f "$COMPOSE_FILE" down -v >/dev/null 2>&1 || true
}
trap cleanup EXIT

rm -rf data1 data2

docker compose -f "$COMPOSE_FILE" up -d --build "${SERVICES[@]}"

# give services a moment to boot before handing over to pytest; tests also wait
sleep 5

"${CMD[@]}"
