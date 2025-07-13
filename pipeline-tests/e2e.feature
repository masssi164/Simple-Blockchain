Feature: Multi-node synchronization

  Scenario: Mining and sync between nodes
    Given two nodes are running
    When I mine a block on node1
    Then node2 should synchronize the block
    And node1 should have a positive balance
    When I send a transaction from node1
    When I mine a block on node1
    Then node2 should synchronize the block
    And both dashboards should load
