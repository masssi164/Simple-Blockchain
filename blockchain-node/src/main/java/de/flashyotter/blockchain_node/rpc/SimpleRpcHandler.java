package de.flashyotter.blockchain_node.rpc;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.serialization.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_rpc.RpcHandler;
import de.flashyotter.blockchain_rpc.RpcResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * JSON-RPC handler exposing a dApp-friendly API surface.
 */
@RequiredArgsConstructor
@Slf4j
public class SimpleRpcHandler implements RpcHandler {

    private static final BigDecimal BASE_UNIT = BigDecimal.valueOf(100_000_000L);
    private static final String CLIENT_VERSION = "simple-chain/" +
            Optional.ofNullable(SimpleRpcHandler.class.getPackage().getImplementationVersion())
                    .orElse("dev");

    private final NodeService node;

    @Override
    public RpcResponse dispatch(String method, JsonNode params) {
        if (method == null || method.isBlank()) {
            return invalidParams("Method name missing");
        }
        RpcMethod rpcMethod = RpcMethod.lookup(method);
        if (rpcMethod == null) {
            log.warn("Unknown RPC method {}", method);
            return RpcResponse.error(-32601, "Method " + method + " not found");
        }
        return rpcMethod.invoke(this, params);
    }

    private RpcResponse handleClientVersion(JsonNode params) {
        return RpcResponse.success(CLIENT_VERSION);
    }

    private RpcResponse handleChainId(JsonNode params) {
        return RpcResponse.success(Numeric.encodeQuantity(BigInteger.valueOf(node.chainId())));
    }

    private RpcResponse handleBlockNumber(JsonNode params) {
        return RpcResponse.success(Numeric.encodeQuantity(BigInteger.valueOf(node.latestHeight())));
    }

    private RpcResponse handleEthGetBalance(JsonNode params) {
        if (params == null || params.isNull() || params.size() == 0) {
            return invalidParams("eth_getBalance requires address parameter");
        }
        String normalized = normalizeAddress(firstParamText(params));
        if (normalized == null) {
            return RpcResponse.success(Numeric.encodeQuantity(BigInteger.ZERO));
        }
        boolean includePending = shouldIncludePending(params.size() > 1 ? params.get(1) : null);
        double balance = addressBalance(normalized, includePending);
        return RpcResponse.success(encodeAmount(balance));
    }

    private RpcResponse handleEthGetBlockByNumber(JsonNode params) {
        if (params == null || params.isNull() || params.size() == 0) {
            return invalidParams("eth_getBlockByNumber requires a block identifier");
        }

        Optional<Block> block = resolveBlock(params.get(0));
        if (block.isEmpty()) {
            return RpcResponse.success(null);
        }

        boolean fullTransactions = params.size() > 1 && params.get(1).asBoolean(false);
        return RpcResponse.success(formatBlock(block.get(), fullTransactions));
    }

    private RpcResponse handleEthSendRawTransaction(JsonNode params) {
        try {
            Transaction tx = parseTransaction(params);
            if (!node.submitTx(tx)) {
                return serverError("Transaction rejected");
            }
            return RpcResponse.success(prefixedHex(tx.calcHashHex()));
        } catch (IllegalArgumentException ex) {
            return invalidParams(ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to handle raw transaction", ex);
            return serverError("Invalid transaction format");
        }
    }

    private RpcResponse handleLegacyGetBalance(JsonNode params) {
        String address = firstParamText(params);
        String normalized = normalizeAddress(address);
        double bal = addressBalance(normalized, true);
        return RpcResponse.success(Map.of("balance", bal));
    }

    private RpcResponse handleLegacyGetBlockByNumber(JsonNode params) {
        Optional<Block> block = resolveBlock(params != null && params.size() > 0 ? params.get(0) : null);
        return block.<RpcResponse>map(RpcResponse::success)
                .orElseGet(() -> RpcResponse.success(Map.of("error", "Block not found")));
    }

    private RpcResponse handleLegacySendTransaction(JsonNode params) {
        RpcResponse response = handleEthSendRawTransaction(params);
        if (response.hasError()) {
            return response;
        }
        Object payload = response.result();
        if (payload instanceof String hash) {
            return RpcResponse.success(Map.of("hash", Numeric.cleanHexPrefix(hash)));
        }
        return RpcResponse.success(payload);
    }

    private Transaction parseTransaction(JsonNode params) {
        if (params == null || params.isNull() || params.size() == 0 || params.get(0).isNull()) {
            throw new IllegalArgumentException("Transaction payload missing");
        }
        String raw = params.get(0).asText();
        String jsonPayload = decodePayload(raw);
        return JsonUtils.txFromJson(jsonPayload);
    }

    private String decodePayload(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Transaction payload missing");
        }
        if (isHexString(trimmed)) {
            try {
                byte[] bytes = Numeric.hexStringToByteArray(prefixedHex(trimmed));
                return new String(bytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid hex payload", e);
            }
        }
        return trimmed;
    }

    private String firstParamText(JsonNode params) {
        if (params == null || params.isNull() || params.size() == 0) {
            return null;
        }
        JsonNode first = params.get(0);
        return first == null || first.isNull() ? null : first.asText();
    }

    private Optional<Block> resolveBlock(JsonNode nodeParam) {
        if (nodeParam == null || nodeParam.isNull()) {
            return Optional.empty();
        }
        if (nodeParam.isTextual()) {
            String tag = nodeParam.asText();
            return switch (tag.toLowerCase()) {
                case "latest", "pending", "finalized", "safe" -> Optional.ofNullable(node.latestBlock());
                case "earliest" -> Optional.ofNullable(node.blockAtHeight(0));
                default -> {
                    Integer height = parseHeight(tag);
                    yield height == null ? Optional.empty() : Optional.ofNullable(node.blockAtHeight(height));
                }
            };
        }
        if (nodeParam.isIntegralNumber()) {
            return Optional.ofNullable(node.blockAtHeight(nodeParam.asInt()));
        }
        return Optional.empty();
    }

    private Integer parseHeight(String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }
        try {
            if (tag.startsWith("0x") || tag.startsWith("0X")) {
                return new BigInteger(tag.substring(2), 16).intValueExact();
            }
            return Integer.parseInt(tag);
        } catch (NumberFormatException | ArithmeticException ex) {
            log.warn("Unable to parse block identifier: {}", tag, ex);
            return null;
        }
    }

    private Map<String, Object> formatBlock(Block block, boolean fullTransactions) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("number", Numeric.encodeQuantity(BigInteger.valueOf(block.getHeight())));
        result.put("hash", prefixedHex(block.getHashHex()));
        result.put("parentHash", prefixedHex(block.getPreviousHashHex()));
        result.put("nonce", Numeric.encodeQuantity(BigInteger.valueOf(block.getNonce())));
        result.put("difficulty", Numeric.encodeQuantity(BigInteger.valueOf(block.getCompactDifficultyBits())));
        result.put("timestamp", Numeric.encodeQuantity(BigInteger.valueOf(TimeUnit.MILLISECONDS.toSeconds(block.getTimeMillis()))));
        result.put("miner", block.getTxList().isEmpty()
                ? null
                : block.getTxList().get(0).getOutputs().isEmpty()
                    ? null
                    : block.getTxList().get(0).getOutputs().get(0).recipientAddress());
        result.put("transactionsRoot", prefixedHex(block.getMerkleRootHex()));

        List<Object> txs = new ArrayList<>();
        for (Transaction tx : block.getTxList()) {
            txs.add(fullTransactions ? formatTransaction(tx) : prefixedHex(tx.calcHashHex()));
        }
        result.put("transactions", txs);
        return result;
    }

    private Map<String, Object> formatTransaction(Transaction tx) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("hash", prefixedHex(tx.calcHashHex()));
        map.put("fee", encodeAmount(tx.getMaxFee()));
        map.put("tip", encodeAmount(tx.getTip()));

        List<Map<String, Object>> inputs = new ArrayList<>();
        for (TxInput in : tx.getInputs()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("address", in.getSender() != null ? AddressUtils.publicKeyToAddress(in.getSender()) : null);
            entry.put("referencedOutput", prefixedHex(in.getReferencedOutputId()));
            inputs.add(entry);
        }
        map.put("inputs", inputs);

        List<Map<String, Object>> outputs = new ArrayList<>();
        for (TxOutput out : tx.getOutputs()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("address", out.recipientAddress());
            entry.put("value", encodeAmount(out.value()));
            outputs.add(entry);
        }
        map.put("outputs", outputs);
        return map;
    }

    private String prefixedHex(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        String normalized = trimmed.startsWith("0x") || trimmed.startsWith("0X")
                ? trimmed
                : Numeric.prependHexPrefix(trimmed);
        return normalized.toLowerCase();
    }

    private boolean shouldIncludePending(JsonNode blockParam) {
        if (blockParam == null || blockParam.isNull()) {
            return false;
        }
        if (!blockParam.isTextual()) {
            return false;
        }
        return "pending".equalsIgnoreCase(blockParam.asText());
    }

    private double addressBalance(String normalizedAddress, boolean includePending) {
        if (normalizedAddress == null) {
            return 0.0;
        }
        Map<String, TxOutput> utxo = includePending
                ? node.currentUtxoIncludingPending()
                : node.currentUtxo();
        return utxo.values().stream()
                .filter(o -> Objects.equals(o.recipientAddress(), normalizedAddress))
                .mapToDouble(TxOutput::value)
                .sum();
    }

    private String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        String trimmed = address.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (AddressUtils.isValid(trimmed)) {
            return trimmed;
        }
        if (isHexString(trimmed)) {
            byte[] raw = Numeric.hexStringToByteArray(prefixedHex(trimmed));
            if (raw.length == 20) {
                try {
                    return AddressUtils.hash160ToAddress(raw);
                } catch (IllegalArgumentException ex) {
                    log.debug("Unable to convert hex address {}", trimmed, ex);
                }
            }
        }
        return trimmed;
    }

    private boolean isHexString(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.startsWith("0x") || value.startsWith("0X") ? value.substring(2) : value;
        if (normalized.length() % 2 != 0) {
            return false;
        }
        for (char c : normalized.toCharArray()) {
            if (Character.digit(c, 16) == -1) {
                return false;
            }
        }
        return true;
    }

    private String encodeAmount(double amount) {
        BigDecimal scaled = BigDecimal.valueOf(amount).multiply(BASE_UNIT);
        BigInteger integer = scaled.setScale(0, RoundingMode.HALF_UP).toBigIntegerExact();
        return Numeric.encodeQuantity(integer);
    }

    private RpcResponse invalidParams(String message) {
        return RpcResponse.error(-32602, message);
    }

    private RpcResponse serverError(String message) {
        return RpcResponse.error(-32000, message);
    }

    private enum RpcMethod {
        CLIENT_VERSION("web3_clientVersion", SimpleRpcHandler::handleClientVersion),
        CHAIN_ID("eth_chainId", SimpleRpcHandler::handleChainId),
        BLOCK_NUMBER("eth_blockNumber", SimpleRpcHandler::handleBlockNumber),
        ETH_GET_BALANCE("eth_getBalance", SimpleRpcHandler::handleEthGetBalance),
        ETH_GET_BLOCK_BY_NUMBER("eth_getBlockByNumber", SimpleRpcHandler::handleEthGetBlockByNumber),
        ETH_SEND_RAW_TRANSACTION("eth_sendRawTransaction", SimpleRpcHandler::handleEthSendRawTransaction),
        LEGACY_GET_BALANCE("getBalance", SimpleRpcHandler::handleLegacyGetBalance),
        LEGACY_GET_BLOCK("getBlockByNumber", SimpleRpcHandler::handleLegacyGetBlockByNumber),
        LEGACY_SEND_TRANSACTION("sendTransaction", SimpleRpcHandler::handleLegacySendTransaction);

        private final String method;
        private final BiFunction<SimpleRpcHandler, JsonNode, RpcResponse> delegate;

        RpcMethod(String method, BiFunction<SimpleRpcHandler, JsonNode, RpcResponse> delegate) {
            this.method = method;
            this.delegate = delegate;
        }

        RpcResponse invoke(SimpleRpcHandler handler, JsonNode params) {
            return delegate.apply(handler, params);
        }

        static RpcMethod lookup(String method) {
            for (RpcMethod value : values()) {
                if (value.method.equals(method)) {
                    return value;
                }
            }
            return null;
        }
    }
}
