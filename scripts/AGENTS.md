The `scripts` directory contains helper shell scripts used during development and CI.

- `check_compose_health.sh` waits until all Docker Compose services report a
  `healthy` status. The script checks containers up to forty times and exits with
  an error if any service remains unhealthy.
- `ci-local.sh` runs a local pipeline: builds the Java backend and UI, executes
  their tests, spins up a two-node Docker Compose environment and runs the Behave
  end-to-end scenario.

Both scripts assume Docker and the compose files at the repository root are
available on the host.

`[CI_REDUNDANT]` - Script used by `make ci-local`; README instead documents a Gradle task.
