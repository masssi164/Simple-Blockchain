Libp2p-specific implementation.

- `Libp2pService` starts the host, exposes `enr()` for the advertised address and
  persists the private key between restarts.
