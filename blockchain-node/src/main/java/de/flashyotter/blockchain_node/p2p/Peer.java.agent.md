Represents a remote blockchain node. Provides the `wsUrl()` helper to build the
WebSocket endpoint and a `fromString` factory to parse `host:port` pairs. Used by
`PeerService` and networking tests.
