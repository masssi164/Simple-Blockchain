GitHub Actions workflows live in this directory.

- `workflows/ci.yml` sets up JDK 17, caches Gradle and runs
  `./gradlew clean jacocoTestReport`. Successful builds upload the node and
  core JARs as artifacts. Artifacts are named `blockchain-node-jar` and `blockchain-core-jar`.
