Feature: Blockchain node integration tests
  Scenario: Disabled integration tests succeed when explicitly enabled
    Given integration tests are forced on
    When the blockchain-node tests run
    Then the build should succeed
