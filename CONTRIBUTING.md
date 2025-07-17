# Contributing to Simple Blockchain

Thank you for your interest in improving this project!

## Local CI

Run the same checks as GitHub Actions locally with:

```bash
./scripts/ci-local.sh
```

The script runs unit tests, builds a reusable runtime image and executes the
end-to-end scenario defined in `pipeline-tests/e2e.feature`.
