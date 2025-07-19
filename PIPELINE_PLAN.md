This file tracks the major steps used to refactor the CI pipeline.

1. Review existing workflow and scripts.
2. Increase startup wait loop to 40 checks to handle slow containers.
3. Replace flaky retry handling with stricter health checks and readiness probes.
4. Align `ci-local.sh` with the workflow so local runs mirror CI.
5. Update documentation to explain the environment variables and new behavior.
