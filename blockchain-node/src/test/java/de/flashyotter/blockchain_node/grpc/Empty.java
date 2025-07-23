package de.flashyotter.blockchain_node.grpc;

/**
 * Empty class for testing gRPC services
 * This is a test stub for com.google.protobuf.Empty
 */
public class Empty {
    private static final Empty DEFAULT_INSTANCE = new Empty();
    
    public static Empty getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        public Empty build() {
            return DEFAULT_INSTANCE;
        }
    }
}
