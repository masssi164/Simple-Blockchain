#!/bin/bash
set -euo pipefail

COMPOSE_FILE=${COMPOSE_FILE:-docker-compose.ci.yml}
ATTEMPTS=40

for ((i=1;i<=ATTEMPTS;i++)); do
    all_healthy=true
    for c in $(docker compose -f "$COMPOSE_FILE" ps -q); do
        status=$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}unknown{{end}}' "$c" 2>/dev/null || echo unknown)
        if [ "$status" != "healthy" ]; then
            all_healthy=false
            break
        fi
    done
    if [ "$all_healthy" = true ]; then
        echo "All services healthy after $i checks"
        exit 0
    fi
    sleep 2
done

echo "Services did not become healthy in time" >&2
docker compose -f "$COMPOSE_FILE" ps || true
docker compose -f "$COMPOSE_FILE" logs || true
exit 1
