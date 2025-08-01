package de.flashyotter.blockchain_rpc;

import com.fasterxml.jackson.databind.JsonNode;

public interface RpcHandler {
    Object dispatch(String method, JsonNode params);
}
