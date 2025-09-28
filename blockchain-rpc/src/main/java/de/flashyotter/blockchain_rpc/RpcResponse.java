package de.flashyotter.blockchain_rpc;

import java.util.Objects;

/**
 * Wrapper returned by {@link RpcHandler} implementations.
 */
public record RpcResponse(Object result, RpcError error) {

    public RpcResponse {
        if (result != null && error != null) {
            throw new IllegalArgumentException("result and error are mutually exclusive");
        }
    }

    public static RpcResponse success(Object result) {
        return new RpcResponse(result, null);
    }

    public static RpcResponse emptySuccess() {
        return success(null);
    }

    public static RpcResponse error(int code, String message) {
        return error(new RpcError(code, message, null));
    }

    public static RpcResponse error(RpcError error) {
        return new RpcResponse(null, Objects.requireNonNull(error));
    }

    public boolean hasError() {
        return error != null;
    }
}
