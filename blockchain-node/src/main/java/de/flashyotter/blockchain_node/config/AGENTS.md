Spring configuration classes.

- Security and JWT setup in `SecurityConfig` and `JwtAuthFilter`.
- Core beans under `NodeBeanConfig` and `CoreConsensusConfig`.
- `WebSocketConfig` exposes STOMP endpoints.
- `NodeProperties` maps application.yml into POJOs.
  It now also configures `libp2pKeyPath` for storing the libp2p private key.

