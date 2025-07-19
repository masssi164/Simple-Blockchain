Feature: Docker Compose configuration

  Scenario: Node2 peers with node1
    When I load the CI compose file
    Then backend2 should configure NODE_PEERS
