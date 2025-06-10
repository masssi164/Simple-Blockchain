Core implementation of the blockchain DAG. Manages all seen blocks and selects
the branch with the most cumulative Proof-of-Work. Also rebuilds the UTXO set
whenever a reorganisation occurs. Used by `NodeService` to append blocks and
obtain difficulty via `nextCompactBits()`.
