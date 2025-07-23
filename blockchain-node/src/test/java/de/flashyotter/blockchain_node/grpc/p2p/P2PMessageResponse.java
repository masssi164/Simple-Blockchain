package de.flashyotter.blockchain_node.grpc.p2p;

/**
 * Test stub for P2PMessageResponse
 */
public class P2PMessageResponse {
    private final String p2PMessageJson;
    private final boolean success;

    private P2PMessageResponse(String p2PMessageJson, boolean success) {
        this.p2PMessageJson = p2PMessageJson;
        this.success = success;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getP2PMessageJson() {
        return p2PMessageJson;
    }

    public boolean getSuccess() {
        return success;
    }

    public static class Builder {
        private String p2PMessageJson = "";
        private boolean success = true;

        public Builder setP2PMessageJson(String p2PMessageJson) {
            this.p2PMessageJson = p2PMessageJson;
            return this;
        }

        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public P2PMessageResponse build() {
            return new P2PMessageResponse(p2PMessageJson, success);
        }
    }
}
