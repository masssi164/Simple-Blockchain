Represents a remote blockchain node. Stores both the REST API port and the
libp2p port plus an optional peer ID. `wsUrl()` builds the WebSocket endpoint
using the REST port while `multiAddr()` derives the libp2p multiaddress.
`fromString` still parses the canonical `host:port` form for convenience.
