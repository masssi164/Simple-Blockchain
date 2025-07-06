Simple‑Chain Node (v0.2‑DEV)

A lean Java 21 + Spring Boot 3 blockchain node that demonstrates a production‑grade architecture in a minimal code base. Proof‑of‑Work consensus, UTXO model, libp2p networking and a reactive REST/WS interface.

Status – BetaThe project recently moved beyond proof‑of‑concept. Expect breaking changes until v1.0.

Feature Matrix

Area

Details

Consensus

Bitcoin‑style PoW, UTXO, compact‑bits difficulty retarget, fork‑choice by total work

Wallet

Local secp256k1 key‑pair stored in encrypted PKCS#12 keystore

Mining

Parallel PoW engine – configurable worker threads

Networking

Dual transport: legacy WebSocket gossipsub and libp2p with Kademlia DHT discovery

Mempool

Fee‑based priority queue with eviction policy

API

Reactive REST + WebSocket push; OpenAPI spec under /swagger-ui.html

CLI & UI

Terminal wallet utility (./gradlew run:cli) and accessible React dashboard

The docker image is < 120 MB and starts in < 2 s on a laptop.

Quick Start with Docker Compose

Requirements – Docker 24+, Docker Compose.

### 1. Environment

Create a .env in the repo root (values are safe defaults):

BACKEND_PORT=1002
FRONTEND_PORT=8892
NODE_LIBP2P_PORT=4001
NODE_PEERS=
NODE_WALLET_PASSWORD=changeMeSuperSecret
NODE_JWT_SECRET=myTopSecret
MINING_THREADS=4            # 0 → auto‑detect
BUILD_CA_CERT=

### 2. Run

./gradlew dockerComposeUp

This builds the backend, UI and starts both containers. Point your browser to http://localhost:$FRONTEND_PORT.

### 3. Stop

./gradlew dockerComposeDown

REST API (excerpt)

GET  /api/wallet                 → address, confirmed balance
GET  /api/wallet/transactions    → last N wallet transactions
POST /api/wallet/send            → create, sign & broadcast TX
POST /api/mining/mine            → mine one block immediately
GET  /api/chain/latest           → current tip
GET  /api/chain/page?page=0&size=5 → paginated blocks (desc)

Fully documented via Swagger / OpenAPI at runtime.

P2P Protocol

The node announces itself on /simple-blockchain/*.

Control – peer list, find‑node, range sync

Blocks  – single blocks (NewBlockDto)

Txs     – raw TX gossip (NewTxDto)

Peer discovery uses Kademlia distance metrics plus optional static seed list.

Roadmap

Next 30 days (v0.2)

LevelDB block store with replay‑safe startup

Harden wire format: length‑prefix, DoS guards

Structured logging + Prometheus metrics

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

