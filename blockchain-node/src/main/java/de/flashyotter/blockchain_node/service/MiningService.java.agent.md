`MiningService` assembles transactions from the `MempoolService` and performs
proof-of-work. `NodeService` invokes `mine()` to create the next block using
the difficulty returned from the `Chain`.
