.PHONY: ci
ci:
	NODE_WALLET_PASSWORD=pass ./gradlew clean test --no-daemon
	cd ui && npm ci && npm test -- --run
	pip install --quiet pytest PyYAML grpcio grpcio-tools requests PyJWT
	pytest -q
