Peer-to-peer networking layer.

- `Peer` represents a remote node (see `Peer.java.agent.md`).
- `Libp2pConfig` wires up libp2p components.
- `P2PProtoMapper` handles protobuf message conversion.
- `libp2p/` contains `Libp2pService` running the network loop and persisting the
  private key configured via `NodeProperties.libp2pKeyPath`.
Handshakes now announce the REST API port and peer ID so nodes can discover each
other without extra lookups.
