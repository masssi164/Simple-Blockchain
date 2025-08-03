.PHONY: ci
ci:
	NODE_WALLET_PASSWORD=pass NODE_JWT_SECRET=changeMeSuperSecret_changeMeSuperSecret ./gradlew clean test --no-daemon
	cd ui && npm ci && npm test -- --run
	pip install --quiet pytest PyYAML grpcio grpcio-tools requests PyJWT
	NODE_JWT_SECRET=changeMeSuperSecret_changeMeSuperSecret pytest -q
