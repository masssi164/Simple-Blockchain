package de.flashyotter.blockchain_node.grpc;

/**
 * Test stub for TransactionRequest
 */
public class TransactionRequest {
    private final String transactionJson;

    private TransactionRequest(String transactionJson) {
        this.transactionJson = transactionJson;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getTransactionJson() {
        return transactionJson;
    }

    public static class Builder {
        private String transactionJson = "";

        public Builder setTransactionJson(String transactionJson) {
            this.transactionJson = transactionJson;
            return this;
        }

        public TransactionRequest build() {
            return new TransactionRequest(transactionJson);
        }
    }
}
