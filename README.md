# Simple-Blockchain (v0.1-SNAPSHOT)

A lightweight Proof-of-Work blockchain node written in **Java 21** + **Spring Boot 3**.

| Feature | Notes |
|---------|-------|
| **Full chain** | UTXO, PoW verification, difficulty retarget |
| **Wallet** | Local ECDSA P-256 key-pair, auto-created on first run |
| **P2P sync** | WebSocket (+ STOMP) gossip with handshake & auto-reorg |
| **CPU miner** | Single-thread demo miner |
| **REST API** | Reactive (WebFlux) – works with `curl`, Postman, etc. |

> **Status — Proof of Concept**
> Not production-ready (no hardening, no fork-choice, no fee market).

---

## Repository Layout

```
blockchain-core/      Java library with consensus and crypto
blockchain-node/      Spring Boot backend using the core library
ui/                   React + Vite front-end
gradle/               Wrapper and version catalog
.github/              CI workflow
startup.py            Starts backend and UI locally
Dockerfile.backend    Build image for the node
Dockerfile.frontend   Build image for the UI
docker-compose.yml    Compose both services
.env                  Runtime configuration for Compose
copyjava.py, copyts.py, copier.sh   Export source snippets
```

The `data/` folder stores LevelDB data generated at runtime. Compose reads environment variables from `.env` and builds images via the Dockerfiles.

---

## Quick Start (with Docker Compose)

**Requirements:**
- [Docker](https://docs.docker.com/get-docker/) must be installed and running.
- [Docker Compose](https://docs.docker.com/compose/) (usually included with Docker Desktop)

**How to run:**

1. **Set your ports (optional):**
   - Edit the `.env` file in the project root to set your desired frontend and backend ports:
     ```env
     BACKEND_PORT=1002
     FRONTEND_PORT=8892
     ```
2. **Start the stack:**
   ```bash
   ./gradlew dockerComposeUp
   ```
   This will build all artifacts, create Docker images, and start the full stack (backend + frontend).
   Alternatively run `python startup.py` to launch the services without Docker.

3. **Stop the stack:**
   ```bash
   ./gradlew dockerComposeDown
   ```

---

## REST API

| Method & Path | Payload | Description |
|---------------|---------|-------------|
| **GET** `/api/wallet` | – | Public key + confirmed balance |
| **GET** `/api/wallet/transactions?address=ADDR&limit=5` | – | Recent wallet transactions |
| **POST** `/api/wallet/send` | `{ "recipient":"<base64>", "amount":1.23 }` | Builds + signs + broadcasts a TX |
| **POST** `/api/tx` | raw `Transaction` JSON | Submit an already-signed TX |
| **POST** `/api/mining/mine` | – | Mine one block immediately |
| **GET** `/api/chain/latest` | – | Latest block header |
| **GET** `/api/chain?from=0` | – | Blocks from a given height |
| **GET** `/api/chain/page?page=0&size=5` | – | Paged block list |

Example:

```bash
# my wallet address & balance
curl http://localhost:1002/api/wallet | jq

# pay 1.0 coin to recipientKey
curl -X POST http://localhost:1002/api/wallet/send \
     -H "Content-Type: application/json" \
     -d '{ "recipient":"MIGbMBAGByqG...", "amount":1.0 }'
```

---

## P2P Protocol (WebSocket `/ws`)

Message types (JSON with `type` discriminator):

* `HANDSHAKE`                  – exchange node id & protocol
* `NEW_TX`, `NEW_BLOCK`        – gossip
* `GET_BLOCKS`, `BLOCKS`       – naïve range sync
* `PEER_LIST`                  – share known peers
* `PING`, `PONG`               – liveness check
* `FIND_NODE`, `NODES`         – Kademlia peer discovery

After exchanging `HANDSHAKE` messages, peers request missing blocks from the
latest height they know. The `Chain` class then re-organises if the received
branch has more cumulative proof-of-work, ensuring both nodes converge on their
last common block.

You can inspect traffic with any WS client:

```
ws://host:port/ws
```

### Chain Synchronization

Nodes keep following peers over WebSocket. After the initial `HANDSHAKE` they
request blocks starting from the local tip. The `Chain` module re-organises to
the branch with the highest cumulative work so that both nodes share the same
history up to the last common block.

---

## Run a Private Network

| Terminal # | Command |
|------------|---------|
| 1 | `BACKEND_PORT=1002 ./gradlew dockerComposeUp` |
| 2 | `BACKEND_PORT=1003 NODE_PEERS=localhost:1002 ./gradlew dockerComposeUp` |

*Nodes discover each other via the seed list and stay in sync.*
Trigger mining on either node; both ledgers will converge.

---

## Road-map

* ~~Fee/priority mempool & eviction~~ (done)
* Better fork-choice (total-work)  
* gRPC / JSON-RPC facade for dApps  
* CLI wallet utility  
* Multi-threaded miner

