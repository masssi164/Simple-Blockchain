.PHONY: ci
ci:
	NODE_MINER_ADDRESS=testMinerAddress NODE_JWT_SECRET=changeMeSuperSecret_changeMeSuperSecret ./gradlew clean test --no-daemon
	cd ui && npm ci && npm test -- --run
	pip install --quiet pytest PyYAML requests PyJWT
	NODE_JWT_SECRET=changeMeSuperSecret_changeMeSuperSecret pytest -q
