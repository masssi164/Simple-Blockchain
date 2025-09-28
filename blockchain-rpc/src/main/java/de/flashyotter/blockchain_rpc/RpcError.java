package de.flashyotter.blockchain_rpc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON-RPC error payload aligned with the 2.0 specification.
 */
public record RpcError(int code, String message, Object data) {

    public RpcError {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }

    public static RpcError of(int code, String message) {
        return new RpcError(code, message, null);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", code);
        payload.put("message", message);
        if (data != null) {
            payload.put("data", data);
        }
        return payload;
    }
}
