package de.flashyotter.blockchain_rpc;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Minimal JSON-RPC 2.0 controller.
 */
@CrossOrigin
@RestController
@RequiredArgsConstructor
@Slf4j
public class JsonRpcController {

    private final RpcHandler handler;

    @PostMapping(value = "/rpc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> handle(@RequestBody JsonNode req) {
        if (!req.has("method") || req.get("method").isNull() || !req.has("id") || req.get("id").isNull()) {
            Map<String, Object> err = new java.util.HashMap<>();
            err.put("jsonrpc", "2.0");
            err.put("error", Map.of(
                    "code", -32600,
                    "message", "Invalid Request: 'method' and 'id' fields are required"
            ));
            err.put("id", req.has("id") ? req.get("id") : null);
            return err;
        }

        String method = req.get("method").asText();
        JsonNode params = req.get("params");
        JsonNode id = req.get("id");
        Object result = handler.dispatch(method, params);
        return Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "result", result
        );
    }
}
