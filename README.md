# Simple-Blockchain (v0.1-SNAPSHOT)

A lightweight Proof-of-Work blockchain node written in **Java 21** + **Spring Boot 3**.

| Feature | Notes |
|---------|-------|
| **Full chain** | UTXO, PoW verification, difficulty retarget |
| **Wallet** | Local ECDSA P-256 key-pair, auto-created on first run |
| **P2P sync** | WebSocket (+ STOMP) gossip of blocks, txs & peers |
| **CPU miner** | Single-thread demo miner |
| **REST API** | Reactive (WebFlux) – works with `curl`, Postman, etc. |

> **Status — Proof of Concept**  
> Not production-ready (no hardening, no fork-choice, no fee market).

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

3. **Stop the stack:**
   ```bash
   ./gradlew dockerComposeDown
   ```

---

## REST API

| Method & Path | Payload | Description |
|---------------|---------|-------------|
| **GET** `/api/wallet` | – | Public key + confirmed balance |
| **POST** `/api/wallet/send` | `{ "recipient":"<base64>", "amount":1.23 }` | Builds + signs + broadcasts a TX |
| **POST** `/api/tx` | raw `Transaction` JSON | Submit an already-signed TX |
| **POST** `/api/mining/mine` | – | Mine one block immediately |
| **GET** `/api/chain/latest` | – | Latest block header |

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

* `NEW_TX`, `NEW_BLOCK`        – gossip
* `GET_BLOCKS`, `BLOCKS`       – naïve range sync
* `PEER_LIST`                  – share known peers
* `PING`, `PONG`               – liveness check
* `FIND_NODE`, `NODES`         – Kademlia peer discovery

You can inspect traffic with any WS client:

```
ws://host:port/ws
```

### Frontend WebSocket Flow

The React UI connects to the node via `VITE_NODE_WS` (e.g. `ws://localhost:3333/ws`).
On connect it sends a `HandshakeDto` and automatically reconnects with
exponential backoff if the socket closes. Only `NewBlockDto` and `NewTxDto`
messages are forwarded to the app.


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

