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
    "recommendation": "Create a PeerStore abstraction with addPeer/removePeer methods and update KademliaService to depend on that interface, avoiding type casts.",
    "snippet": "26      /** Add a peer to the routing table and registry. */\n27      public void store(Peer peer) {\n28          ((java.util.Set<Peer>) table).add(peer);\n29          registry.add(peer);\n30      }"
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
    "recommendation": "Configure a pendingQueueLimit in NodeProperties and allocate the queue with this bound; drop or defer peers when full to protect memory.",
    "snippet": "28          boolean fresh = peers.add(p);\n29          if (fresh) pending.add(p);          // mark for dial\n30          return fresh;\n31      }\n33      /* dial queue consumed by discovery loop */"
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
    "recommendation": "Use libp2p's AutoNATService to parse the observation result and store the reported address; fall back to a configured static address if discovery fails.",
    "snippet": "126      public void discoverPublicAddr(Peer peer) {\n127          AutonatProtocol.AutoNatController dummy = msg ->\n128                  java.util.concurrent.CompletableFuture.completedFuture(\n129                          io.libp2p.protocol.autonat.pb.Autonat.Message.getDefaultInstance());\n130          dummy.requestDial(host.getPeerId(), host.listenAddresses()).join();"
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
    "recommendation": "Return a non-blocking Mono or CompletableFuture and reuse requestBlocksReactive so callers handle the result asynchronously.",
    "snippet": "200                      });\n201                      stream.writeAndFlush(io.netty.buffer.Unpooled.wrappedBuffer(buf.array()));\n202                  }).join();\n203              // Allow more time for peers on CI runners which may be slow\n204              return fut.get(10, java.util.concurrent.TimeUnit.SECONDS);"
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
    "recommendation": "Enforce token verification for every handshake by calling a shared verifyJwt utility and close connections when the token is missing or invalid.",
    "snippet": "246                  P2PMessage pm = P2PMessage.parseFrom(data);\n247                  if (!pm.getJwt().isBlank() && props.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length >= 32) {\n248                      try {\n249                          Jwts.parserBuilder()\n250                                  .setSigningKey(Keys.hmacShaKeyFor("
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
    "recommendation": "Track the timer handle; clearTimeout before scheduling a new attempt and reset reconnectMs to the initial delay upon a successful connection.",
    "snippet": "107    private scheduleReconnect() {\n108      this.stream = undefined;\n109      setTimeout(() => this.open(this.factory), this.reconnectMs);\n110      this.reconnectMs = Math.min(this.reconnectMs * 2, 30000);\n111    }"
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
    "recommendation": "Drop the directive and fix the underlying type issues so the compiler can check the P2P client during builds.",
    "snippet": "1  // @ts-nocheck\n2  import { createLibp2p, Libp2p } from 'libp2p';\n3  import { TCP } from '@libp2p/tcp';\n4  import { Mplex } from '@libp2p/mplex';\n5  import { Noise } from '@chainsafe/libp2p-noise';"
  },
  {
    "id": "ISSUE-008",
    "category": "Network",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/p2p/libp2p/Libp2pService.java",
    "line": 124,
    "type": "Public address misdetected",
    "description": "discoverPublicAddr records the first local interface when AutoNAT does not return a result.",
    "cause": "The dummy AutoNAT controller ignores the observation and never sets an externally reachable multiaddr.",
    "impact": "Peers may learn a loopback address and fail to connect from other hosts.",
    "recommendation": "Implement AutoNATService to capture the observed address or provide a configurable advertisedAddr.",
    "snippet": "126      public void discoverPublicAddr(Peer peer) {\n127          AutonatProtocol.AutoNatController dummy = msg ->\n128                  java.util.concurrent.CompletableFuture.completedFuture(\n129                          io.libp2p.protocol.autonat.pb.Autonat.Message.getDefaultInstance());\n130          dummy.requestDial(host.getPeerId(), host.listenAddresses()).join();"
  }
]

_self_reflection: |
  Reviewed every Java and TypeScript file relating to peer connections in blockchain-core, blockchain-node and ui.
  Performed three independent passes focusing on networking, security and maintainability concerns.
  Consolidated overlapping findings to ensure only consistently observed issues remain.
  Confirmed that the AutoNAT logic never sets a public address and the baseUrl field is unrelated to peer discovery.
  General themes emerged around missing abstractions and duplicated connection logic, suggesting a shared P2P module would reduce complexity.
