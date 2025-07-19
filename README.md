# Simple-Chain Node (v0.2-DEV)

A lean Java&nbsp;21 and Spring Boot&nbsp;3 blockchain node demonstrating a modern architecture in a concise code base. It implements Proof-of-Work mining, a UTXO ledger and libp2p networking. Clients interact via REST, WebSocket or gRPC.

**Status: Beta** – breaking changes may occur until v1.0.

## Recent additions
- Fee market with base fee and optional transaction tips
- Periodic UTXO snapshots with pruning
- HD wallet derived from a mnemonic seed
- REST API secured with JWT tokens
- Optional Noise encryption for libp2p
- Prometheus metrics exported at `/actuator/prometheus`
- gRPC API for chain, wallet and mining operations
- Compose tasks `composeUp` and `composeDown` manage Docker
- Write-ahead log for replay-safe LevelDB storage
- Compressed UTXO snapshots tracked via manifest
- Structured JSON logging with Prometheus metrics
- P2P messages encoded with protobuf

## Feature matrix

| Area | Details |
|------|---------|
| Consensus | Bitcoin-style PoW, UTXO model, compact-bits difficulty retarget, fork choice by total work |
| Wallet | HD wallet stored in encrypted PKCS#12 keystore |
| Mining | Parallel PoW engine with configurable worker threads |
| Networking | WebSocket gossip and libp2p with Kademlia DHT, optional Noise encryption |
| Mempool | Fee-based priority queue with base fee and tips |
| API | Reactive REST & WebSocket push, JWT secured, Prometheus metrics |
| UI | React dashboard using REST and gRPC clients |

The Docker image is under 120&nbsp;MB and starts in less than two seconds on a laptop.

## Quick start with Docker Compose

**Requirements** – Docker&nbsp;24+ and Docker Compose.

### 1. Environment

Create a `.env` file in the repo root with values similar to:

```
BACKEND_PORT=1002
FRONTEND_PORT=8892
NODE_P2P_MODE=dual
NODE_LIBP2P_PORT=4001
NODE_LIBP2P_ENCRYPTED=false
NODE_PEERS=
NODE_DATA_PATH=data
NODE_WALLET_PASSWORD=changeMeSuperSecret
NODE_JWT_SECRET=myTopSecret
VITE_NODE_JWT_SECRET=myTopSecret
VITE_NODE_GRPC=localhost:9090
NODE_MINING_THREADS=4
NODE_SNAPSHOT_INTERVAL_SEC=300
NODE_HISTORY_DEPTH=1000
NODE_GRPC_PORT=9090
BUILD_CA_CERT=
```

Blocks reside under `${NODE_DATA_PATH}/blocks`. Mount this directory to preserve the chain between restarts.

### 2. Run

```
./gradlew composeUp
```

This builds the backend and UI and then launches both containers. Browse to `http://localhost:$FRONTEND_PORT`.

### Multiple nodes

Give each instance its own data and wallet folder:

```yaml
volumes:
  - ./data1:${NODE_DATA_PATH}
  - ./wallet1:/root/.simple-chain
```

Use `data2`/`wallet2` etc. for additional nodes.

### 3. Connect peers

Set `NODE_PEERS` to a comma-separated list of multiaddresses and expose `NODE_LIBP2P_PORT` so others can dial your node.

### 4. Stop

```
./gradlew composeDown
```

## REST API (excerpt)

```
GET  /api/wallet                 → address, confirmed balance
GET  /api/wallet/transactions    → last N wallet transactions
POST /api/wallet/send            → create, sign & broadcast TX
POST /api/mining/mine            → mine one block immediately
GET  /api/chain/latest           → current tip
GET  /api/chain/page?page=0&size=5 → paginated blocks (desc)
```

The full API is documented via Swagger / OpenAPI at runtime.

### gRPC API

Set `NODE_GRPC_PORT` to expose the same endpoints over gRPC (defaults to `9090`). Service definitions reside in `blockchain-node/src/main/proto` and are used by the React UI for low-latency communication.

## P2P protocol

The node announces itself on `/simple-blockchain/*`. All handshake data is encoded using the protobuf definitions in `p2p.proto`.

- Control – peer list, find-node, range sync
- Blocks  – single block messages
- Txs     – raw transaction gossip

Peer discovery uses Kademlia distance metrics plus optional static seeds.
The SyncService retries block range requests, which improves stability when
peers temporarily fail to respond.

## CI pipeline

GitHub Actions run Gradle and UI tests on every pull request. A Docker Compose
setup with two nodes powers the end-to-end tests. The workflow installs JDK and
Node, caches dependencies and then packages the Spring Boot app with
`bootJar`. Selenium is started alongside the services for a full integration
test. Each backend container declares `SERVER_PORT` so the health checks run
inside Docker Compose succeed.
Backend2 now waits for backend1's health endpoint before it starts so the
initial peer connection is reliable.
The workflow waits up to forty health checks before running end-to-end tests
to accommodate slow CI runners. If mining occasionally times out, the tests
retry when the `FLAKY_RETRY=1` environment variable is set.

## Contributing

Fork the repo, create a branch, run `./gradlew verify` and open a PR.

## Run CI locally

Execute the same checks that GitHub Actions runs with:

```bash
make ci-local
```
The script builds the runtime image, starts the Compose setup and sets
`FLAKY_RETRY=1` so the end-to-end scenario is resilient to transient mining
timeouts.

## Roadmap

The planned improvements for upcoming releases are outlined in
[ROADMAP.md](ROADMAP.md).

## License

MIT – see [LICENSE](LICENSE).
