[
  {
    "id": "ISSUE-001",
    "category": "Maintainability",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/service/KademliaService.java",
    "line": 27,
    "type": "Unsafe cast",
    "description": "KademliaService.store casts the routing table to a Set before adding peers.",
    "cause": "The implementation assumes KademliaRoutingTable implements Set and performs a raw cast.",
    "impact": "Future library changes could trigger ClassCastException at runtime, breaking peer discovery.",
    "recommendation": "Introduce a PeerStore interface or expose an addPeer method on the routing table so KademliaService can add peers without raw casts.",
    "snippet": """
26      /** Add a peer to the routing table and registry. */
27      public void store(Peer peer) {
28          ((java.util.Set<Peer>) table).add(peer);
29          registry.add(peer);
30      }
"""
  },
  {
    "id": "ISSUE-002",
    "category": "Performance",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/service/PeerRegistry.java",
    "line": 27,
    "type": "Unbounded queue",
    "description": "PeerRegistry keeps new peers in an unbounded LinkedBlockingQueue.",
    "cause": "The pending dial queue has no size limit or backpressure.",
    "impact": "A malicious peer list could exhaust memory by flooding pending entries.",
    "recommendation": "Configure a bounded queue size (e.g. via NodeProperties) and refuse or rate-limit peers when the pending dial queue is full.",
    "snippet": """
31      }
32
33      /* dial queue consumed by discovery loop */
34      private final java.util.concurrent.BlockingQueue<Peer> pending =
35              new java.util.concurrent.LinkedBlockingQueue<>();
"""
  },
  {
    "id": "ISSUE-003",
    "category": "Network",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/p2p/libp2p/Libp2pService.java",
    "line": 126,
    "type": "Incomplete AutoNAT",
    "description": "discoverPublicAddr starts an AutoNAT dial but discards the result.",
    "cause": "The dummy controller ignores the response and simply records the first local listen address.",
    "impact": "Nodes behind NAT may advertise the wrong public address and become unreachable.",
    "recommendation": "Implement a real AutoNAT client that parses the remote dial response and caches the discovered address instead of assuming the first listen address.",
    "snippet": """
127          AutonatProtocol.AutoNatController dummy = msg ->
128                  java.util.concurrent.CompletableFuture.completedFuture(
129                          io.libp2p.protocol.autonat.pb.Autonat.Message.getDefaultInstance());
130          dummy.requestDial(host.getPeerId(), host.listenAddresses()).join();
131          if (publicAddr == null && !host.listenAddresses().isEmpty()) {
"""
  },
  {
    "id": "ISSUE-004",
    "category": "Performance",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/p2p/libp2p/Libp2pService.java",
    "line": 202,
    "type": "Blocking call",
    "description": "requestBlocks waits synchronously on a future with fut.get().",
    "cause": "The method joins the libp2p stream and then blocks for up to ten seconds.",
    "impact": "Blocking threads reduce scalability and may stall the event loop under load.",
    "recommendation": "Return a CompletableFuture or Mono instead of blocking; merge this logic with requestBlocksReactive to keep the libp2p pipeline non-blocking.",
    "snippet": """
200                      });
201                      stream.writeAndFlush(io.netty.buffer.Unpooled.wrappedBuffer(buf.array()));
202                  }).join();
203              // Allow more time for peers on CI runners which may be slow
204              return fut.get(10, java.util.concurrent.TimeUnit.SECONDS);
"""
  },
  {
    "id": "ISSUE-005",
    "category": "Security",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/p2p/libp2p/Libp2pService.java",
    "line": 246,
    "type": "Unauthenticated handshake",
    "description": "ControlHandler accepts handshakes even when no JWT is provided.",
    "cause": "JWT verification only occurs if the message contains a non-empty token.",
    "impact": "Any peer can join the network without authentication, enabling Sybil attacks.",
    "recommendation": "Reject handshake frames without a JWT; centralize token verification in a shared filter so both ControlHandler and send() enforce the same rule.",
    "snippet": """
246                  P2PMessage pm = P2PMessage.parseFrom(data);
247                  if (!pm.getJwt().isBlank() && props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 32) {
248                      try {
249                          Jwts.parserBuilder()
250                                  .setSigningKey(Keys.hmacShaKeyFor(
"""
  },
  {
    "id": "ISSUE-006",
    "category": "Network",
    "module": "ui",
    "file": "ui/src/api/p2p.ts",
    "line": 107,
    "type": "Reconnect loop",
    "description": "scheduleReconnect schedules a new timeout without clearing older ones.",
    "cause": "The function uses setTimeout but does not store or cancel prior timers when reconnecting.",
    "impact": "Repeated connection failures may spawn multiple concurrent connection attempts.",
    "recommendation": "Track the reconnect timer id and clearTimeout each time; reset reconnectMs back to the initial value once the connection succeeds.",
    "snippet": """
107    private scheduleReconnect() {
108      this.stream = undefined;
109      setTimeout(() => this.open(this.factory), this.reconnectMs);
110      this.reconnectMs = Math.min(this.reconnectMs * 2, 30000);
111    }
"""
  },
  {
    "id": "ISSUE-007",
    "category": "Maintainability",
    "module": "ui",
    "file": "ui/src/api/p2p.ts",
    "line": 1,
    "type": "TypeScript disabled",
    "description": "The P2P client disables TypeScript checks with // @ts-nocheck.",
    "cause": "Type checking is bypassed to avoid compilation errors.",
    "impact": "Future refactoring may introduce subtle bugs that the compiler would normally catch.",
    "recommendation": "Remove the directive and fix type errors to keep static guarantees intact.",
    "snippet": """
1  // @ts-nocheck
2  import { createLibp2p, Libp2p } from 'libp2p';
3  import { TCP } from '@libp2p/tcp';
4  import { Mplex } from '@libp2p/mplex';
5  import { Noise } from '@chainsafe/libp2p-noise';
"""
  }
]

_self_reflection: |
  Reviewed every Java and TypeScript file relating to peer connections in blockchain-core, blockchain-node and ui.
  Performed three independent passes focusing on networking, security and maintainability concerns.
  Consolidated overlapping findings to ensure only consistently observed issues remain.
  General themes emerged around missing abstractions and duplicated connection logic, suggesting a shared P2P module would reduce complexity.
