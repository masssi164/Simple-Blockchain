This file tracks the major steps used to refactor the CI pipeline.

1. Removed obsolete Behave pipeline tests and the `ci-local.sh` script.
2. Added Python regression tests under `tests/` executed via `make ci`.
3. Updated GitHub Actions workflow to call `make ci` directly.
