# Changelog

## Unreleased
- Pipeline Green Story: added local CI script and improved sync waiting in E2E tests.
- Docker Compose uses prebuilt `simple-blockchain-node:runtime` image for faster startup.
- Backend2 waits for backend1 health before starting.
- Peer sync jobs run on boundedElastic scheduler to avoid blocking other tasks.
