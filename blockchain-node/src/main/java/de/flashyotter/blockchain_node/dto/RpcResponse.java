package de.flashyotter.blockchain_node.dto;

/** Generic JSON-RPC response. */
public record RpcResponse(
        String jsonrpc,
        String id,
        Object result,
        RpcError error) {

    /** Error object as defined by JSON-RPC. */
    public record RpcError(int code, String message) {}
}
