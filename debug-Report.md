[
  {
    "id": "ISSUE-001",
    "category": "Security",
    "module": "docker-compose",
    "file": "docker-compose.yml",
    "line": "40-44",
    "type": "SecretExposure",
    "description": "Frontend build receives backend JWT secret via build args",
    "cause": "compose passes NODE_JWT_SECRET to VITE_NODE_JWT_SECRET",
    "impact": "Exposes server authentication secret to all clients",
    "recommendation": "Do not include NODE_JWT_SECRET in frontend build arguments",
    "snippet": "40:  VITE_NODE_URL: http://localhost:${BACKEND_PORT}/api\n41:  VITE_NODE_LIBP2P: /ip4/127.0.0.1/tcp:${NODE_LIBP2P_PORT}\n42:  VITE_NODE_GRPC: localhost:${NODE_GRPC_PORT}\n43:  VITE_NODE_JWT_SECRET: ${NODE_JWT_SECRET}"
  },
  {
    "id": "ISSUE-002",
    "category": "Security",
    "module": "ui",
    "file": "src/api/p2p.ts",
    "line": "82-108",
    "type": "MissingVerification",
    "description": "P2P client processes incoming messages without validating JWT",
    "cause": "readLoop() decodes messages but ignores the jwt field",
    "impact": "Malicious peers can inject forged data into the UI",
    "recommendation": "Verify and reject messages with invalid or missing JWT",
    "snippet": "86: const P2PMessage = root.p2p.P2PMessage;\n87: for await (const buf of pipe(this.stream.source, lp.decode())) {\n88:   const msg = P2PMessage.decode($protobuf.Reader.create(buf));\n89:   if (msg.newBlock || msg.newTx) {\n90:     const dto: P2PMessage = msg.newBlock"
  },
  {
    "id": "ISSUE-003",
    "category": "Network",
    "module": "ui + blockchain-node",
    "file": "src/api/p2p.ts + p2p/libp2p/Libp2pService.java",
    "line": "66-75 + 258-265",
    "type": "InvalidHandshake",
    "description": "UI sends handshake with constant nodeId and zero ports which the server stores as a peer",
    "cause": "sendHandshake() hardcodes 'ui-client' and ports 0 while ControlHandler blindly stores peers",
    "impact": "Kademlia table fills with unreachable entries and wasted dials",
    "recommendation": "Differentiate UI clients or ignore handshakes lacking valid ports",
    "snippet": "ui/src/api/p2p.ts lines 66-75 and Libp2pService.java lines 258-265"
  },
  {
    "id": "ISSUE-004",
    "category": "Performance",
    "module": "blockchain-node",
    "file": "p2p/libp2p/Libp2pService.java",
    "line": "313-338",
    "type": "BlockingCall",
    "description": "send() waits on CompletableFuture.join which may block caller threads",
    "cause": "fut.thenAccept(...).join() inside send method",
    "impact": "Slow or unreachable peers stall broadcast operations",
    "recommendation": "Use asynchronous send with timeout instead of blocking join",
    "snippet": "313: private void send(...){\n324: ByteBuffer buf...\n331: fut = host.newStream...\n336: byte[] data = buf.array();\n337: fut.thenAccept(...).join();" 
  },
  {
    "id": "ISSUE-005",
    "category": "Maintainability",
    "module": "blockchain-node",
    "file": "p2p/Peer.java",
    "line": "24-30",
    "type": "DeadCode",
    "description": "Method wsUrl() remains even though WebSocket transport was removed",
    "cause": "Legacy API helper never called anywhere",
    "impact": "Confuses developers about supported transports",
    "recommendation": "Delete wsUrl() or repurpose it if WebSocket support returns",
    "snippet": "24: public Peer(String host, int restPort, int libp2pPort, ... )\n26: /** WebSocket URL... */\n27: public String wsUrl() {\n28:   return \"ws://\" + host + ':' + restPort + \"/ws\";\n29: }"
  },
  "_self_reflection": "All modules under blockchain-node, blockchain-core, ui plus .env and compose file were scanned for peer connection logic. I reviewed Java and TypeScript source for security checks, handshake management, and network flows in three passes, ensuring overlapping issues were consistently observed."
]
