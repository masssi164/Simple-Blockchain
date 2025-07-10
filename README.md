Simple‑Chain Node (v0.2‑DEV)

A lean Java 21 + Spring Boot 3 blockchain node that demonstrates a production‑grade architecture in a minimal code base. Proof‑of‑Work consensus, UTXO model, libp2p networking and a reactive REST/WS interface.

Status – Beta
The project recently moved beyond proof-of-concept. Expect breaking changes until v1.0.
## Recent additions
- Fee market with base fee calculation and optional transaction tips.
- Periodic UTXO snapshots with automatic pruning.
- HD wallet derived from a mnemonic seed phrase.
- REST API secured with JWT tokens.
- libp2p supports optional Noise encryption.
- Prometheus metrics exported at `/actuator/prometheus`.
- gRPC API for chain, wallet and mining operations.
- Write-ahead log for replay-safe LevelDB block storage.
- UTXO snapshots compressed and tracked via manifest.
- Structured JSON logging with additional Prometheus metrics.
- Handshake broadcasts the node's public address for autodiscovery.
- P2P messages encoded with protobuf.

Feature Matrix

Area

Details

Consensus

Bitcoin‑style PoW, UTXO, compact‑bits difficulty retarget, fork‑choice by total work

Wallet

HD wallet with mnemonic stored in encrypted PKCS#12 keystore

Mining

Parallel PoW engine – configurable worker threads

Networking

Dual transport: legacy WebSocket gossipsub and libp2p with Kademlia DHT discovery; optional Noise encryption

Mempool

Fee-based priority queue with base fee and tip sorting

API

Reactive REST + WebSocket push; JWT secured; Prometheus metrics at /actuator/prometheus
CLI & UI

Terminal wallet utility (./gradlew run:cli) and accessible React dashboard

The docker image is < 120 MB and starts in < 2 s on a laptop.

Quick Start with Docker Compose

Requirements – Docker 24+, Docker Compose.

### 1. Environment

Create a .env in the repo root (values are safe defaults):

BACKEND_PORT=1002
FRONTEND_PORT=8892
NODE_P2P_MODE=dual                  # legacy | libp2p | dual
NODE_LIBP2P_PORT=4001
NODE_LIBP2P_ENCRYPTED=false         # true to enable Noise
NODE_PEERS=
NODE_DATA_PATH=data
NODE_WALLET_PASSWORD=changeMeSuperSecret
NODE_JWT_SECRET=myTopSecret
VITE_NODE_JWT_SECRET=myTopSecret   # same secret for UI
VITE_NODE_GRPC=localhost:9090      # gRPC address for UI
NODE_MINING_THREADS=4               # 0 → auto-detect
NODE_SNAPSHOT_INTERVAL_SEC=300
NODE_HISTORY_DEPTH=1000
NODE_GRPC_PORT=9090
BUILD_CA_CERT=

Blocks are kept in a LevelDB database under `${NODE_DATA_PATH}/blocks`. Mount
this directory when running the Docker image so the chain persists across
restarts.

### 2. Run

./gradlew dockerComposeUp

This builds the backend, UI and starts both containers. Point your browser to http://localhost:$FRONTEND_PORT.

### Multiple nodes

To run several nodes on one host give each instance its own data and wallet
folder. Map these directories under `backend` so the LevelDB store stays
persistent:

```yaml
volumes:
  - ./data1:${NODE_DATA_PATH}
  - ./wallet1:/root/.simple-chain
```

Use `data2`/`wallet2` etc. for additional nodes.

### 3. Connect peers
Set `NODE_PEERS` to a comma-separated list of multiaddresses.
Expose `NODE_LIBP2P_PORT` so other nodes can dial your instance.

### 4. Stop

./gradlew dockerComposeDown

REST API (excerpt)

GET  /api/wallet                 → address, confirmed balance
GET  /api/wallet/transactions    → last N wallet transactions
POST /api/wallet/send            → create, sign & broadcast TX
POST /api/mining/mine            → mine one block immediately
GET  /api/chain/latest           → current tip
GET  /api/chain/page?page=0&size=5 → paginated blocks (desc)

Fully documented via Swagger / OpenAPI at runtime.

### gRPC API

Set `NODE_GRPC_PORT` to expose the same endpoints over gRPC (defaults to
`9090`). Service definitions live under `blockchain-node/src/main/proto` and
cover mining, wallet and chain queries.

P2P Protocol

The node announces itself on /simple-blockchain/*.
Handshake messages include the node's public address for easier discovery.
All data is encoded using protobuf definitions in `p2p.proto`.

Control – peer list, find‑node, range sync

Blocks  – single block messages

Txs     – raw TX gossip

Peer discovery uses Kademlia distance metrics plus optional static seed list.

Roadmap
Next 30 days (v0.2)
Harden wire format: length‑prefix, DoS guards
CLI wallet (send, balance, history)
Accessibility audit of the React UI
Q4 2025 (v0.3)
Replace JSON over WS with protobuf over gRPC
Compact block relay + thin‑client (SPV) mode
Adaptive fee market, child‑pays‑for‑parent
Snapshots + UTXO compaction
### v1.0
Formal security review & fuzzing harness
Ledger pruning / archival node split
Governance upgrade support (BIP‑9‑style soft forks)
Inter‑chain bridge PoC (IBC‑inspired)
Contributing
Fork, create a branch, run ./gradlew verify and open a PR.
License

MIT – see LICENSE.

