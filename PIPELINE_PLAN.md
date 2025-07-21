This file tracks the major steps used to refactor the CI pipeline.

1. Removed obsolete Behave pipeline tests and the `ci-local.sh` script.
2. Added Python regression tests under `tests/` executed via `make ci`.
3. Updated GitHub Actions workflow to call `make ci` directly.
4. Removed Docker health checks from Compose and the workflow. Containers now
   start without curl and logs are captured after tests.
5. Dropped Behave from the Makefile so only pytest runs during CI.
