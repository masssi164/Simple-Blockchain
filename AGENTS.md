This repository contains a proof-of-concept blockchain implementation. The following directories host all source code:

- `blockchain-core` – Java library with blockchain data structures and consensus logic.
- `blockchain-node` – Spring Boot application that exposes REST and WebSocket APIs and coordinates mining, mempool and networking.
- `ui` – React + TypeScript front‑end.
- `gradle` – Gradle wrapper and dependency version catalog.

Other items include test resources under each module, a helper script `copyui`, and persistent `data` for LevelDB blocks.
