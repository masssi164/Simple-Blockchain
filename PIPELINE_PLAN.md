This file tracks the major steps used to refactor the CI pipeline.

1. Removed obsolete Behave pipeline tests and the `ci-local.sh` script.
2. Added Python regression tests under `tests/` executed via `make ci`.
3. Updated GitHub Actions workflow to call `make ci` directly.
4. Removed Docker health checks from Compose and the workflow. Containers now
   start without curl and logs are captured after tests.
5. Dropped Behave from the Makefile so only pytest runs during CI.
6. Regression: the Python e2e test `test_e2e_compose` failed because `/api`
   requests lacked the required JWT bearer token. Spring Security therefore
   returned HTTP 403 and the balance assertions never passed. Fix by minting an
   HS256 token inside the test using `NODE_JWT_SECRET` and attaching it to
   `Authorization` headers.

7. Observed issues ranked by severity after re-running the workflow:
   - **Critical** – Missing JWT header in `test_e2e_compose` (fixed in step 6).
   - **High** – Docker Compose logs are not collected when pytest fails,
     obscuring backend crashes. Consider streaming `docker compose logs` on
     teardown for quicker triage.
   - **Medium** – `make ci` reinstalls Python dependencies on every run instead
     of using a virtualenv or caching layer, which adds ~7s locally and longer
     in CI.
