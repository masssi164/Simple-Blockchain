package de.flashyotter.blockchain_rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Minimal JSON-RPC 2.0 controller.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class JsonRpcController {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RpcHandler handler;

    @PostMapping(value = "/rpc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> handle(@RequestBody JsonNode req) {
        String method = req.get("method").asText();
        JsonNode params = req.get("params");
        Object id = req.get("id");
        Object result = handler.dispatch(method, params);
        return Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "result", result
        );
    }
}
