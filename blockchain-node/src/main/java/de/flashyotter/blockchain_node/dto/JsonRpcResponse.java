package de.flashyotter.blockchain_node.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON-RPC 2.0 response wrapper.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonRpcResponse(String jsonrpc, Object result, JsonRpcResponseError error, Object id) {

    public static JsonRpcResponse success(Object id, Object result) {
        return new JsonRpcResponse("2.0", result, null, id);
    }

    public static JsonRpcResponse error(Object id, int code, String message, Object data) {
        return new JsonRpcResponse("2.0", null, new JsonRpcResponseError(code, message, data), id);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record JsonRpcResponseError(int code, String message, Object data) { }
}
