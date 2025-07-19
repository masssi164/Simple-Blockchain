Feature: Build tool regression

  Scenario: Gradle wrapper executes
    When I run "./gradlew help"
    Then the command should succeed

  Scenario: NPM tests run
    Given working directory "ui"
    When I run "npm test -- --run"
    Then the command should succeed
