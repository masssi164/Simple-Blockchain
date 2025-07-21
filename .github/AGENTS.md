This directory hosts the GitHub Actions workflow defined in
`workflows/ci.yml`. The workflow invokes `make ci` to run all tests.

CI_REDUNDANT: Removed the explicit Python install step from the workflow since
`make ci` already installs the same dependencies.

