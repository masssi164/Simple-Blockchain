This directory contains helper shell scripts used during development and CI.

- `check_compose_health.sh` waits until all Docker Compose services report a
  `healthy` status. The script checks containers up to forty times and exits with
  an error if any service remains unhealthy.
