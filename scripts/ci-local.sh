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

# Run lightweight regression checks
pip install behave PyYAML >/tmp/pip.log
behave pipeline-tests/compose_config.feature pipeline-tests/regression.feature

# Build runtime image and start multi-node setup
docker build -t simple-blockchain-node:runtime -f Dockerfile.backend .
COMPOSE_FILE=docker-compose.ci.yml
# Start first node and frontend
docker compose -f $COMPOSE_FILE up -d --build backend1 frontend1
./scripts/check_compose_health.sh
# Query peer ID of backend1 and start second node
PEER_ID=$(curl -s http://localhost:3333/node/peer-id | jq -r .peerId)
BACKEND1_MULTIADDR="/dns4/backend1/tcp/4001/p2p/${PEER_ID}"
BACKEND1_MULTIADDR="$BACKEND1_MULTIADDR" docker compose -f $COMPOSE_FILE up -d backend2 frontend2
./scripts/check_compose_health.sh

# Run end-to-end tests
pip install selenium requests PyJWT behave grpcio protobuf
behave pipeline-tests/e2e.feature
STATUS=$?

docker compose -f $COMPOSE_FILE down
exit $STATUS
