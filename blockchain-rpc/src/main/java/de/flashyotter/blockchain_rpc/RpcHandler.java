package de.flashyotter.blockchain_rpc;

import com.fasterxml.jackson.databind.JsonNode;

public interface RpcHandler {
    RpcResponse dispatch(String method, JsonNode params);
}
