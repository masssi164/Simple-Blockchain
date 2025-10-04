This file tracks the major steps used to refactor the CI pipeline.

1. Removed obsolete Behave pipeline tests and the `ci-local.sh` script.
2. Added Python regression tests under `tests/` executed via `make ci`.
3. Updated GitHub Actions workflow to call `make ci` directly.
4. Removed Docker health checks from Compose and the workflow. Containers now
   start without curl and logs are captured after tests.
5. Dropped Behave from the Makefile so only pytest runs during CI.
6. Regression: the new Python e2e test `test_e2e_compose` expects the
   Docker Compose stack from `docker-compose.ci.yml` to be running, but the
   current workflow only executes `make ci` (Gradle, npm and pytest) without
   starting Compose. As a result `wait_for_rpc` fails to reach
   `http://localhost:3333/rpc`. Task: restore an automated `docker compose up`
   plus health check step before pytest (either in `make ci` or the workflow) so
   the services are available during the test suite.
