The `pipeline-tests` directory holds Behave BDD tests for this project.

Features:
- `e2e.feature` – launches two nodes and ensures blocks and transactions
  propagate between them. Uses Selenium to verify the web dashboards.
- `compose_config.feature` – validates environment variables in
  `docker-compose.ci.yml`.
- `regression.feature` – sanity checks that Gradle and NPM commands run.

Step definitions reside in `steps/`:
- `e2e_steps.py` connects via gRPC to the nodes and drives Selenium.
- `regression_steps.py` executes CLI commands and inspects the compose file.

Generated gRPC stubs `node_pb2*.py` are kept here for the tests.

Run a scenario with `behave pipeline-tests/<feature>`.
