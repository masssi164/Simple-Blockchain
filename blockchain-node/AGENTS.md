`blockchain-node` contains the Spring Boot application built on `blockchain-core`.

Important paths under `src/main/java/de/flashyotter/blockchain_node`:
- `BlockchainNodeApplication.java` – entry point starting the HTTP & WebSocket server.
- `bootstrap/StartupInitializer.java` – tasks executed at startup.
- `config/` – Spring configuration classes (`SecurityConfig`, `WebSocketConfig`, ...).
- `controller/` – REST controllers for chain, mining, transactions and wallet.
- `service/` – business logic (`NodeService`, `MiningService`, etc.).
- `p2p/` – `Peer`, `PeerClient` and `PeerServer` for WebSocket networking.
- `storage/` – `BlockStore` with LevelDB and in-memory implementations.
- `wallet/` – wallet and keystore utilities.

Resources in `src/main/resources` define application defaults. Tests in
`src/test` cover controllers, services and networking.

```plantuml
@startuml
NodeService -> MiningService: mine()
MiningService -> Chain: difficulty + latest block
NodeService -> P2PBroadcastService: broadcastBlock()
@enduml
```
