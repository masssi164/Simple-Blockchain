# dApp gRPC API

The node exposes a lightweight gRPC surface for decentralised applications. It
is defined in [`blockchain-node/src/main/proto/node.proto`](../blockchain-node/src/main/proto/node.proto)
and served by the Spring Boot application on `NODE_GRPC_PORT` (defaults to
`9090`). The UI consumes the same API via the protobuf.js generated clients.

## Authentication

If `NODE_JWT_SECRET` is configured the server expects a Bearer token on every
call. Tokens can be created by signing an empty payload with the shared secret,
mirroring the behaviour in `ui/src/api/grpc.ts`.

## Services

### `Chain`

| Method | Request | Response | Description |
| --- | --- | --- | --- |
| `Latest` | `Empty` | `Block` | Returns the most recent block. |
| `Page` | `PageRequest` | `BlockList` | Provides simple pagination over the
  canonical chain. |

### `Mining`

| Method | Request | Response | Description |
| --- | --- | --- | --- |
| `Mine` | `Empty` | `Block` | Mines a new block using the pending mempool
  transactions. |

### `Wallet`

| Method | Request | Response | Description |
| --- | --- | --- | --- |
| `Send` | `SendRequest` | `Transaction` | Submits a signed transaction to the
  mempool. |
| `Info` | `Empty` | `WalletInfo` | Returns the local wallet address and its
  confirmed balance. |
| `History` | `HistoryRequest` | `TxList` | Streams recent wallet transactions.

## Data model

Messages mirror the Java domain objects. For instance, `Block` contains the
height, hash metadata, and a list of transactions (each with `TxInput` and
`TxOutput` entries). All monetary values use floating point numbers to match the
existing wallet implementation.

## Error handling

gRPC status codes are surfaced directly to the client. Application level
failures—such as rejected transactions—propagate as `INVALID_ARGUMENT` errors
with descriptive messages from the backend services.
