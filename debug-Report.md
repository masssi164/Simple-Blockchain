[
  {
    "id": "ISSUE-001",
    "category": "Security",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/config/NodeProperties.java",
    "line": 64,
    "type": "Hardcoded secret",
    "description": "Default JWT secret and wallet password are hardcoded",
    "cause": "Configuration class initializes sensitive defaults in source",
    "impact": "Nodes may run with predictable credentials, enabling token forgery and wallet compromise",
    "recommendation": "Load secrets exclusively from environment or secure storage and require non-default values",
    "snippet": """
64      /**
65       * Password for encrypting/decrypting the PKCS12 keystore.
66       */
67      private String walletPassword;
68
69      /** Shared secret used to sign and verify JWT tokens. */
70      private String jwtSecret = "changeMeSuperSecret";
"""
  },
  {
    "id": "ISSUE-002",
    "category": "Security",
    "module": "repo root",
    "file": ".env",
    "line": 6,
    "type": "Credentials in repository",
    "description": "Sample environment file exposes wallet password and JWT secret",
    "cause": "Sensitive variables are checked into version control",
    "impact": "Leaked credentials risk unauthorized node or wallet access",
    "recommendation": "Store example secrets separately and document secure configuration practices",
    "snippet": """
1  BACKEND_PORT=1002
2  FRONTEND_PORT=8892
3  NODE_LIBP2P_PORT=4001
4  NODE_PEERS=
5  NODE_DATA_PATH=data
6  NODE_WALLET_PASSWORD=changeMeSuperSecret
7  NODE_JWT_SECRET=myTopSecret
"""
  },
  {
    "id": "ISSUE-003",
    "category": "Maintainability",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/service/KademliaService.java",
    "line": 26,
    "type": "Reflection usage",
    "description": "Routing table updates rely on reflective invocation",
    "cause": "Store() searches methods at runtime to call add()",
    "impact": "Reflection obscures intent and may break with library updates",
    "recommendation": "Use the public routing table API directly or wrap it with a stable interface",
    "snippet": """
25      /** Add a peer to the routing table and registry. */
26      public void store(Peer peer) {
27          try {
28              java.lang.reflect.Method target = null;
29              for (var m : table.getClass().getMethods()) {
30                  if (m.getName().equals("add") && m.getParameterCount() == 1 && m.getReturnType() != boolean.class) {
31                      target = m; break;
"""
  },
  {
    "id": "ISSUE-004",
    "category": "Maintainability",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/p2p/P2PProtoMapper.java",
    "line": 1,
    "type": "Code duplication",
    "description": "Two distinct P2PProtoMapper implementations exist",
    "cause": "Both p2p and grpc packages define similar mapping logic",
    "impact": "Duplicated serializers diverge over time and increase bug risk",
    "recommendation": "Consolidate protobuf mapping in a single shared utility",
    "snippet": """
1  package de.flashyotter.blockchain_node.p2p;
2
3  import blockchain.core.model.Block;
4  import blockchain.core.model.Transaction;
5  import de.flashyotter.blockchain_node.dto.*;
"""
  },
  {
    "id": "ISSUE-005",
    "category": "Network",
    "module": "blockchain-node",
    "file": "blockchain-node/src/main/java/de/flashyotter/blockchain_node/service/P2PService.java",
    "line": 50,
    "type": "Improper handshake",
    "description": "Handshake response returns base URL as node ID without validation",
    "cause": "Service builds reply using props.getBaseUrl() instead of verifying the peer",
    "impact": "Peers may misidentify each other leading to inconsistent routing",
    "recommendation": "Return a structured handshake containing verified node identifiers",
    "snippet": """
50                  case HANDSHAKE:
51                      Handshake hs = message.getHandshake();
52                      // Just validate node ID format for now
53                      boolean valid = hs.getNodeId() != null && !hs.getNodeId().isEmpty();
54                      if (!valid) {
"""
  },
  {
    "id": "ISSUE-006",
    "category": "Performance",
    "module": "ui",
    "file": "ui/src/api/p2p.ts",
    "line": 106,
    "type": "Unbounded backoff",
    "description": "Reconnect delay doubles indefinitely and never resets on success",
    "cause": "scheduleReconnect multiplies reconnectMs without resetting after a stable connection",
    "impact": "Temporary network glitches can lead to very long reconnection delays for the UI",
    "recommendation": "Reset the backoff timer once a connection is established",
    "snippet": """
106    private scheduleReconnect() {
107      this.stream = undefined;
108      setTimeout(() => this.open(this.factory), this.reconnectMs);
109      this.reconnectMs = Math.min(this.reconnectMs * 2, 30000);
110    }
"""
  },
  "_self_reflection": "Reviewed blockchain-core, blockchain-node and ui directories focusing on P2P connection code in Java and TypeScript. Checked .env and docker-compose for exposed secrets. Examined Libp2p service, peer management classes, and frontend p2p.ts to ensure coverage of handshake, discovery and reconnection logic across modules."
]
