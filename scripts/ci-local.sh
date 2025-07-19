#!/bin/bash
set -euo pipefail

# Local CI simulation for Simple Blockchain

# Run backend unit tests and build jar
./gradlew clean bootJar jacocoTestReport --no-daemon

# Install UI dependencies and run tests
pushd ui > /dev/null
npm ci
npm run test -- --run
popd > /dev/null

# Build runtime image and start multi-node setup
docker build -t simple-blockchain-node:runtime -f Dockerfile.backend .
COMPOSE_FILE=docker-compose.ci.yml
docker compose -f $COMPOSE_FILE up -d --build

# Wait for containers to become healthy
for i in {1..40}; do
  if docker compose -f $COMPOSE_FILE ps | grep -q "healthy"; then
    break
  fi
  sleep 5
done

# Run end-to-end tests
pip install selenium requests PyJWT behave grpcio protobuf
FLAKY_RETRY=1 behave pipeline-tests/e2e.feature
STATUS=$?

docker compose -f $COMPOSE_FILE down
exit $STATUS
