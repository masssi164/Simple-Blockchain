# Simple-Blockchain

A proof-of-concept blockchain written in **Java 21** with a small React front-end. It features a minimal Proof-of-Work chain, WebSocket based peer-to-peer sync and a wallet secured by an ECDSA key pair. The project is split into separate modules for the core logic, the Spring Boot node and the UI.

## Features

| Feature | Notes |
|---------|-------|
| **Full chain** | UTXO set, PoW verification, difficulty retarget |
| **Wallet** | Local P-256 key pair generated on first run |
| **P2P sync** | STOMP/WebSocket gossip, `HANDSHAKE` and auto re-org |
| **CPU miner** | Simple single-threaded miner |
| **REST API** | Reactive WebFlux endpoints |

> **Status – Proof of Concept**
> This project is a demo. It lacks fork-choice rules, a fee market and hardening for production use.

## Repository layout

```
blockchain-core/      Java library with consensus and crypto
blockchain-node/      Spring Boot backend using the core library
ui/                   React + Vite front-end
gradle/               Wrapper and version catalog
.github/              CI workflow
startup.py            Runs backend + UI locally
Dockerfile.backend    Build image for the node
Dockerfile.frontend   Build image for the UI
docker-compose.yml    Compose both services
.env                  Runtime configuration for Compose
copyjava.py, copyts.py, copier.sh   Export source snippets
```

Runtime data (blocks, wallet keys) lives in `data/`. Compose reads configuration such as ports and secrets from `.env`.

## Getting started

### With Docker Compose

1. **Adjust ports (optional)** – Edit `.env` to change the exposed ports:
   ```env
   BACKEND_PORT=1002
   FRONTEND_PORT=8892
   ```
   The backend automatically queries `api.ipify.org` to advertise its public IP
   to peers.
2. **Start** – build artifacts and run both services:
   ```bash
   ./gradlew composeUp
   ```
   When finished you can reach the API on `http://localhost:$BACKEND_PORT/api` and the UI on `http://localhost:$FRONTEND_PORT`.
3. **Stop** – shut down the containers:
   ```bash
   ./gradlew composeDown
   ```

### Local development

The backend can also run directly from Gradle and the UI via Vite:

```bash
# start Spring Boot node
./gradlew :blockchain-node:bootRun

# in another terminal run the React dev server
cd ui && npm install && npm run dev
```
Alternatively, build the executable JAR and use the convenience script:

```bash
./gradlew :blockchain-node:bootJar
python startup.py
```

### Tests

Run all unit tests and generate coverage reports with:

```bash
./gradlew clean jacocoTestReport
```

## REST API

| Method & Path | Payload | Description |
|---------------|---------|-------------|
| **GET** `/api/wallet` | – | Public key and balance |
| **GET** `/api/wallet/transactions?address=ADDR&limit=5` | – | Recent wallet tx |
| **POST** `/api/wallet/send` | `{ "recipient":"<base64>", "amount":1.23 }` | Build, sign and broadcast a TX |
| **POST** `/api/tx` | raw `Transaction` JSON | Submit an already-signed TX |
| **POST** `/api/mining/mine` | – | Mine one block immediately |
| **GET** `/api/chain/latest` | – | Latest block header |
| **GET** `/api/chain?from=0` | – | Blocks from a given height |
| **GET** `/api/chain/page?page=0&size=5` | – | Paged block list |

Example usage:

```bash
# query wallet
curl http://localhost:1002/api/wallet | jq

# pay 1 coin to recipientKey
curl -X POST http://localhost:1002/api/wallet/send \
     -H "Content-Type: application/json" \
     -d '{ "recipient":"MIGbMBAGByqG...", "amount":1.0 }'
```

## WebSocket protocol (`/ws`)

Nodes communicate over WebSockets and exchange JSON messages:

* `HANDSHAKE` – share node ID and protocol version
* `NEW_TX`, `NEW_BLOCK` – gossip
* `GET_BLOCKS`, `BLOCKS` – naïve range sync
* `PEER_LIST` – share known peers
* `PING`, `PONG` – liveness check
* `FIND_NODE`, `NODES` – minimal Kademlia discovery

After the initial `HANDSHAKE`, peers request any missing blocks. The `Chain` module re‑organises to the branch with the most cumulative work so that all nodes agree on history up to their last common block.

To inspect traffic, connect with any WebSocket client:

```
ws://localhost:$BACKEND_PORT/ws
```

## Run a private network

Use different ports and specify peers when launching multiple nodes:

| Terminal | Command |
|----------|---------|
| 1 | `BACKEND_PORT=1002 ./gradlew composeUp` |
| 2 | `BACKEND_PORT=1003 NODE_PEERS=localhost:1002 ./gradlew composeUp` |

Start mining on either node and watch both chains converge.
Each node detects its public host automatically via `api.ipify.org`.

## Roadmap

* ~~Fee/priority mempool~~
* Better fork choice (total work)
* gRPC/JSON‑RPC API for dApps
* CLI wallet utility
* Multi‑threaded miner

