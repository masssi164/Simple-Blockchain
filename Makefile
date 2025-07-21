.PHONY: ci
ci:
	./gradlew clean test --no-daemon
	cd ui && npm ci && npm test -- --run
	pip install --quiet pytest behave PyYAML grpcio grpcio-tools requests
	pytest -q
	behave -q tests/features
