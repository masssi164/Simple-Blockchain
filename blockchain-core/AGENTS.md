`blockchain-core` is a standalone Java library that implements the core blockchain logic.

Directories:
- `src/main/java/blockchain/core` contains all source packages:
  - `model` – block, transaction and wallet classes
  - `consensus` – chain data structure and difficulty retarget
  - `crypto` – helpers for hashing and addresses
  - `mempool` – in-memory transaction pool
  - `serialization` – JSON utilities
  - `exceptions` – domain-specific runtime exception
- `src/test/java` provides unit tests for these packages.

Build settings live in `build.gradle` and the module can be built independently.
