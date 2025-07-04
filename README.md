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
      # Optional certificate to trust during the Docker build
      BUILD_CA_CERT=./zscaler.crt
      ```
    If `BUILD_CA_CERT` is empty or the file can't be found, nothing is imported.
    Docker BuildKit (`DOCKER_BUILDKIT=1`) must be enabled for the secret mount.
    `docker-compose` passes `BACKEND_PORT` to the backend container as `SERVER_PORT`.
    The Docker image exposes this port and defaults to `3333` if not overridden.
2. **Start the stack:**
   ```bash
   ./gradlew dockerComposeUp
   ```
   This will build all artifacts, create Docker images, and start the full stack (backend + frontend).

3. **Stop the stack:**
   ```bash
   ./gradlew dockerComposeDown
   ```

### Zertifikat (optional)

Setze `BUILD_CA_CERT` auf den absoluten Pfad zu deinem
Zscaler-Root-Zertifikat **in Unix-Schreibweise**:

Unix / Linux
```bash
export BUILD_CA_CERT=/opt/certs/zscaler.crt
```

Windows (PowerShell)
```powershell
setx BUILD_CA_CERT "C:/Users/maierm/zscaler/zscalerwsl.crt"
```

Hinweise:

- Verwende absolute Pfade in **Unix-Schreibweise**, z. B. `C:/Users/maierm/zscaler/zscaler.crt`.
- Ist `BUILD_CA_CERT` leer, wird nichts importiert.
- Docker BuildKit (`DOCKER_BUILDKIT=1`) muss aktiviert sein.
- Beim Docker-Build wird das Zertifikat zuerst mit
  `keytool -cacerts` in die Standard-Trust-Store importiert. Schlägt das fehl,
  legt der Build einen beschreibbaren Truststore unter
  `/etc/ssl/certs/java/cacerts` an und importiert das Zertifikat dort.

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
curl http://localhost:$BACKEND_PORT/api/wallet | jq

# pay 1.0 coin to recipientKey
curl -X POST http://localhost:$BACKEND_PORT/api/wallet/send \
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

The React UI connects to the node via `VITE_NODE_WS` (e.g. `ws://localhost:$BACKEND_PORT/ws`).
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

