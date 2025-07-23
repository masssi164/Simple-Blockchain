# Debugging Report

## P2P Configuration Mismatch

- `.env` sets `NODE_P2P_MODE=legacy` which implies a WebSocket based peer transport.
- `NodeProperties` also defaults to `p2pMode` = "legacy" but this value is unused in the application.
- The codebase only implements a libp2p service; there is no WebSocket P2P server.
- Running the node with the default configuration therefore leaves it without any active peer transport.

Relevant lines:
```
.env line 9: NODE_P2P_MODE=legacy
NodeProperties.java line 44-47: p2pMode = "legacy"
```

## Missing WebSocket Endpoint

- The UI's `NodeWs` helper expects a `/ws` endpoint derived from the `VITE_NODE_WS` variable.
- `docker-compose.yml` passes `VITE_NODE_WS: ws://localhost:${BACKEND_PORT}/ws` to the frontend build.
- `WebSocketConfig` exposes only a STOMP endpoint at `/stomp`; no handler registers `/ws`.
- Consequently the browser tries to open a WebSocket that the backend does not provide.

Relevant lines:
```
NodeWs (ws.ts) lines 26-28
docker-compose.yml line 42-45
WebSocketConfig.java lines 29-32
Peer.java lines 26-29
```

## Consensus Checks

- `Chain.validateTxs` verifies signatures and prevents double spends but does not check that transaction inputs cover their outputs.
- Only the mempool performs this balance check. Blocks composed outside the mempool could bypass it.

Relevant lines:
```
Chain.java lines 174-226
```

## Mempool Concurrency

- `Mempool.add` checks for double spends using an iteration over the current pool but the check and insertion are not atomic. Under high concurrency conflicting transactions might pass the check before being stored.

## Mining Service Result Handling

- `MiningService.mine` returns `result.get()` from the worker threads without null checking. If the pool shuts down early or no thread finds a block, callers may receive `null` and trigger an NPE.

## Front‑End Peer Connection

- Because the backend lacks a `/ws` endpoint and only exposes libp2p, the UI cannot establish a real-time connection. The WebSocket helper should be replaced or reconfigured to use libp2p or to remove the unused legacy transport.

## Test Execution

Running `make ci` after installing JDK 17 still results in failing tests from the `blockchain-node` module:
```
107 tests completed, 13 failed, 10 skipped
FAILURE: Build failed with an exception.
```
See `/tmp/make_ci.log` for details.

## Latest Fixes

- Replaced the front-end WebSocket client with a libp2p implementation.
- Generated protobuf bindings provide the libp2p message schema.
- Removed the unused `p2pMode` property from `NodeProperties`.
- Updated Docker and `.env` to expose `VITE_NODE_LIBP2P` instead of `VITE_NODE_WS`.
- Added unit tests confirming the libp2p client connects and the UI build succeeds.
