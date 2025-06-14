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

## 1  Build & Run

```bash
# 1  compile everything
./gradlew :blockchain-node:bootJar

# 2  start the node (default port 8080)
java -jar blockchain-node/build/libs/blockchain-node-0.0.1-SNAPSHOT.jar
```

First start creates:

* `data/wallet.json`    ← local key-pair (Base64-encoded)
* `data/blocks/`        ← LevelDB store for blocks
* `data/nodeId`         ← persistent node identifier

Peers listed under `node.peers` act as bootstrap seeds for peer discovery.

---

## 2  REST API

| Method & Path | Payload | Description |
|---------------|---------|-------------|
| **GET** `/api/wallet` | – | Public key + confirmed balance |
| **POST** `/api/wallet/send` | `{ "recipient":"&lt;base64&gt;", "amount":1.23 }` | Builds + signs + broadcasts a TX |
| **POST** `/api/tx` | raw `Transaction` JSON | Submit an already-signed TX |
| **POST** `/api/mining/mine` | – | Mine one block immediately |
| **GET** `/api/chain/latest` | – | Latest block header |

Example:

```bash
# my wallet address & balance
curl http://localhost:8080/api/wallet | jq

# pay 1.0 coin to recipientKey
curl -X POST http://localhost:8080/api/wallet/send \
     -H "Content-Type: application/json" \
     -d '{ "recipient":"MIGbMBAGByqG...", "amount":1.0 }'
```

---

## 3  P2P Protocol (WebSocket `/ws`)

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

---

## 4  Run a Private Network

| Terminal # | Command |
|------------|---------|
| 1 | `java -jar …jar --server.port=8080` |
| 2 | `java -jar …jar --server.port=8081 --node.peers=localhost:8080` |

*Nodes discover each other via the seed list and stay in sync.*
Trigger mining on either node; both ledgers will converge.

---

## 5  Road-map

* ~~Fee/priority mempool & eviction~~ (done)
* Better fork-choice (total-work)  
* gRPC / JSON-RPC facade for dApps  
* CLI wallet utility  
* Multi-threaded miner

For Scrum user stories see [docs/scrum_user_stories.md](docs/scrum_user_stories.md).

---
