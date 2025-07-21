# Changelog

## Unreleased
- Pipeline Green Story: added local CI script and improved sync waiting in E2E tests.
- Removed Behave-based pipeline and local CI script in favour of `make ci`.
- Docker Compose builds `simple-blockchain-node:runtime` if not present to avoid missing-image errors.
- Backend2 waits for backend1 health before starting.
