package de.flashyotter.blockchain_node.rpc;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_rpc.RpcHandler;

import java.util.Map;

/**
 * Basic RPC handler bridging to NodeService.
 */
@RequiredArgsConstructor
@Slf4j
public class SimpleRpcHandler implements RpcHandler {

    private final NodeService node;

    @Override
    public Object dispatch(String method, JsonNode params) {
        return switch (method) {
            case "getBalance" -> {
                String address = params.get(0).asText();
                double bal = addressBalance(address);
                yield Map.of("balance", bal);
            }
            case "getBlockByNumber" -> {
                int height = params.get(0).asInt();
                Block b = node.blocksFromHeight(height).stream().findFirst().orElse(null);
                yield b != null ? b : Map.of("error", "Block not found");
            }
            case "sendTransaction" -> {
                String raw = params.get(0).asText();
                try {
                    Transaction tx = blockchain.core.serialization.JsonUtils.txFromJson(raw);
                    node.submitTx(tx);
                    yield Map.of("hash", tx.calcHashHex());
                } catch (Exception e) {
                    log.error("Failed to deserialize transaction from JSON: {}", raw, e);
                    yield Map.of("error", "Invalid transaction format");
                }
            }
            default -> {
                log.warn("Unknown RPC method {}", method);
                yield Map.of("error", "Unknown method");
            }
        };
    }

    private double addressBalance(String address) {
        return node.currentUtxoIncludingPending().values().stream()
                .filter(o -> o.recipientAddress().equals(address))
                .mapToDouble(blockchain.core.model.TxOutput::value)
                .sum();
    }
}
