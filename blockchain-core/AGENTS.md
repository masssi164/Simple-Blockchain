`blockchain-core` is a standalone Java library implementing the fundamental blockchain data structures and algorithms. It maintains a DAG of all seen blocks and automatically re-organises to the branch with the most cumulative work. The module builds a library JAR consumed by `blockchain-node`.

Packages under `src/main/java/blockchain/core`:
- `consensus` – `Chain.java` manages the block DAG and difficulty, while
  `ConsensusParams.java` defines PoW constants.
- `crypto` – utilities like `AddressUtils`, `CryptoUtils` and `HashingUtils`.
- `model` – `Block`, `Transaction`, `Wallet` and related classes.
- `mempool` – simple in-memory `Mempool` for pending transactions.
- `serialization` – JSON helpers (`JsonUtils`).
- `exceptions` – custom runtime `BlockchainException`.

Tests in `src/test/java` mirror these packages. Build logic resides in
`build.gradle`.
