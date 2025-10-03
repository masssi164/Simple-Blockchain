# dApp JSON-RPC API

The node exposes an Ethereum-inspired JSON-RPC 2.0 surface on
`http://<host>:<port>/rpc`. It is powered by
[`JsonRpcController`](../blockchain-node/src/main/java/de/flashyotter/blockchain_node/controller/JsonRpcController.java)
and consumed by the React UI and browser wallets such as MetaMask.

## Authentication

If `NODE_JWT_SECRET` is configured the UI includes a Bearer token with each
request. External clients can do the same by signing an empty payload with the
shared secret, mirroring the behaviour in `ui/src/api/jsonRpc.ts`.

## Methods

### Ethereum-compatible

| Method | Params | Result | Description |
| --- | --- | --- | --- |
| `web3_clientVersion` | none | string | Identifies the node implementation. |
| `net_version` | none | string | Returns the chain ID as a decimal string. |
| `eth_chainId` | none | hex string | Chain ID encoded as a quantity. |
| `eth_blockNumber` | none | hex string | Latest block height. |
| `eth_getBlockByNumber` | `[blockTag, fullTx?]` | object \| null | Fetches a block by tag (`latest`, `earliest`) or height. |
| `eth_getBlockByHash` | `[hash, fullTx?]` | object \| null | Fetches a block by hash. |
| `eth_accounts` | none | string[] | Returns the local wallet address. |
| `eth_getBalance` | `[address]` | hex string | Returns the account balance in base units. |
| `eth_sendTransaction` | `[tx]` | hash | Creates and broadcasts a transaction from the local wallet. |

### SimpleBlockchain extensions

| Method | Params | Result | Description |
| --- | --- | --- | --- |
| `sb_mineBlock` | none | `BlockView` | Mines a block immediately. |
| `sb_chainLatest` | none | `BlockView` | Returns the tip of the chain. |
| `sb_chainPage` | `[page, size]` | `BlockView[]` | Provides descending pagination over the chain. |
| `sb_walletInfo` | none | object | Returns address, confirmed balance and pending deltas. |

`BlockView` mirrors the Java domain model and includes the block height, hashes,
timestamp, difficulty bits, nonce, merkle root and the raw transactions.

## Error handling

Errors follow the JSON-RPC 2.0 specification. Invalid requests or parameters
return errors with codes `-32600` to `-32602`. Unexpected failures result in
`-32603` alongside a descriptive message. All responses include the request `id`
so clients can match them with outstanding calls.
