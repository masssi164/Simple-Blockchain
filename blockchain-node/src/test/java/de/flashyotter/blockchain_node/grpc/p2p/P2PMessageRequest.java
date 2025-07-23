package de.flashyotter.blockchain_node.grpc.p2p;

/**
 * Test stub for P2PMessageRequest
 */
public class P2PMessageRequest {
    private String p2PMessageJson;

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getP2PMessageJson() {
        return p2PMessageJson;
    }

    private P2PMessageRequest(String p2PMessageJson) {
        this.p2PMessageJson = p2PMessageJson;
    }

    public static class Builder {
        private String p2PMessageJson;

        public Builder setP2PMessageJson(String p2PMessageJson) {
            this.p2PMessageJson = p2PMessageJson;
            return this;
        }

        public P2PMessageRequest build() {
            return new P2PMessageRequest(p2PMessageJson);
        }
    }
}
