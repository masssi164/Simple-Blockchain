Represents a remote blockchain node. Stores the REST API port, the libp2p port
and an optional peer ID. `multiAddr()` derives the libp2p multiaddress while
`fromString` parses the canonical `host:port` form for convenience.
