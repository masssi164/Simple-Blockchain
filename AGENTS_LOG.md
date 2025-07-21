## Health check stabilization
- Replaced incorrect $$SERVER_PORT references in docker-compose.ci.yml.
- Added readiness probes via Spring Boot property.
- Introduced scripts/check_compose_health.sh to verify container health.
- Removed FLAKY_RETRY handling and updated tests and docs accordingly.
- Deprecated local pipeline scripts and switched CI to `make ci`.
