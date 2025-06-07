`blockchain-node` hosts the Spring Boot application exposing the blockchain over REST and WebSockets. Key directories in `src/main/java/de/flashyotter/blockchain_node`:
- `config` – Spring configuration and properties
- `controler` – REST controllers for chain, transactions, mining and wallet
- `service` – application services coordinating the core library, P2P network and mining
- `p2p` – WebSocket peer client/server implementation
- `storage` – interfaces and implementations for persisting blocks (LevelDB or in-memory)
- `wallet` – key store and wallet utilities
- `bootstrap` – startup tasks executed when the node launches

Resources under `src/main/resources` contain the YAML configuration. Tests mirror the package structure under `src/test`.
