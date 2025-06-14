package de.flashyotter.blockchain_node.dto;

import java.util.Map;

/** Basic JSON-RPC request. */
public record RpcRequest(
        String jsonrpc,
        String method,
        Map<String, Object> params,
        String id) {}
