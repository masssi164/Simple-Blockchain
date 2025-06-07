Services coordinating blockchain operations live in this package.

- `NodeService` – central orchestrator for chain, mempool and mining
- `MiningService` – CPU miner producing new blocks
- `MempoolService` – manages pending transactions
- `P2PBroadcastService` / `P2PBroadcastPort` – send events to peers
- `PeerService` / `PeerRegistry` – track and discover peers
- `DiscoveryLoop` and `SyncService` – maintain network connectivity and sync state
