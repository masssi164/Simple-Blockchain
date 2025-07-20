Spring configuration classes.

- Security and JWT setup in `SecurityConfig` and `JwtAuthFilter`.
- Core beans under `NodeBeanConfig` and `CoreConsensusConfig`.
- `WebSocketConfig` exposes STOMP endpoints.
- `NodeProperties` maps application.yml into POJOs.

Errors
------
- `[LOGIC_BAD_ADDR]` - `NodeProperties` splits `NODE_PEERS` by `:` and converts the second
  segment to an int. Multiaddresses like `/dns4/node/tcp/4001/p2p/ID` therefore
  cause a `NumberFormatException` during startup.
