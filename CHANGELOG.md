# Changelog

## Unreleased
- Pipeline Green Story: added local CI script and improved sync waiting in E2E tests.
- Removed Behave-based pipeline and local CI script in favour of `make ci`.
- Docker Compose uses prebuilt `simple-blockchain-node:runtime` image for faster startup.
- Backend2 waits for backend1 health before starting.
