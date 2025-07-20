Step definitions for Behave features.

- `e2e_steps.py` provides helpers to wait for gRPC services, mine blocks, send
  transactions and verify the dashboard via Selenium.
- `regression_steps.py` runs shell commands and parses `docker-compose.ci.yml` to
  ensure environment variables are set correctly.
