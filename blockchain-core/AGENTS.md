`blockchain-core` is a standalone Java library implementing the fundamental
blockchain data structures and algorithms.

Packages under `src/main/java/blockchain/core`:
- `consensus` – `Chain.java` manages the block DAG and difficulty, while
  `ConsensusParams.java` defines PoW constants.
- `crypto` – utilities like `AddressUtils`, `CryptoUtils` and `HashingUtils`.
- `model` – `Block`, `Transaction`, `Wallet` and related classes.
- `mempool` – simple in-memory `Mempool` for pending transactions.
- `serialization` – JSON helpers (`JsonUtils`).
- `exceptions` – custom runtime `BlockchainException`.

Unit tests live under `src/test/java/simple/blockchain` mirroring these packages.
They cover consensus rules, wallet operations and mempool edge cases.

`build.gradle` configures JDK 21 toolchains and generates JaCoCo reports.
Proto definitions in `blockchain-node/src/main/proto` map these models for gRPC.
