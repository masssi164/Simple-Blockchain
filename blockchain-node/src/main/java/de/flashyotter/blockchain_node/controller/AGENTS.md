REST controllers for public APIs.

- `ChainController`, `MiningController`, `TxController`, `WalletController` expose JSON endpoints.
- `NodeController` provides node management operations including `/node/enr`.
- `SnapshotController` serves UTXO snapshot files to peers.
- `RestErrorHandler` converts exceptions into HTTP responses.
