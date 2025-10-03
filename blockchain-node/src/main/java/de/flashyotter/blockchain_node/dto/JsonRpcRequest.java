package de.flashyotter.blockchain_node.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Minimal JSON-RPC 2.0 request payload.
 */
public record JsonRpcRequest(String jsonrpc, String method, List<Object> params, Object id) {

    @JsonCreator
    public JsonRpcRequest(@JsonProperty("jsonrpc") String jsonrpc,
                          @JsonProperty("method") String method,
                          @JsonProperty("params") List<Object> params,
                          @JsonProperty("id") Object id) {
        this.jsonrpc = jsonrpc == null ? "2.0" : jsonrpc;
        this.method = method;
        this.params = params == null ? java.util.List.of() : List.copyOf(params);
        this.id = id;
    }
}
