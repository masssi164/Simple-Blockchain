package de.flashyotter.blockchain_node.controller;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.dto.BlockView;
import de.flashyotter.blockchain_node.dto.JsonRpcRequest;
import de.flashyotter.blockchain_node.dto.JsonRpcResponse;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON-RPC facade exposing Ethereum-inspired methods so browser wallets such as
 * MetaMask can connect to the node. Custom {@code sb_*} methods cover features
 * unique to this project (mining, chain pagination, extended wallet info).
 */
@RestController
@RequestMapping(value = "/rpc", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class JsonRpcController {

    private static final BigDecimal COIN_SCALE      = new BigDecimal("100000000"); // 1 coin = 1e8 base units
    private static final Duration   MINE_TIMEOUT    = Duration.ofMinutes(5);

    private final NodeService   node;
    private final WalletService wallet;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public JsonRpcResponse handle(@RequestBody JsonRpcRequest request) {
        if (request.method() == null || request.method().isBlank()) {
            return JsonRpcResponse.error(request.id(), JsonRpcError.INVALID_REQUEST.code,
                    "Method must be provided", null);
        }

        try {
            Object result = switch (request.method()) {
                case "web3_clientVersion" -> "SimpleBlockchain/1.0";
                case "net_version" -> Long.toString(node.chainId());
                case "eth_chainId" -> encodeQuantity(node.chainId());
                case "eth_blockNumber" -> encodeQuantity(node.latestHeight());
                case "eth_getBlockByNumber" -> getBlockByNumber(request.params());
                case "eth_getBlockByHash" -> getBlockByHash(request.params());
                case "eth_accounts" -> List.of(localAddress());
                case "eth_getBalance" -> getBalance(request.params());
                case "eth_sendTransaction" -> sendTransaction(request.params());
                case "sb_mineBlock" -> mineBlock();
                case "sb_chainLatest" -> BlockView.from(node.latestBlock());
                case "sb_chainPage" -> chainPage(request.params());
                case "sb_walletInfo" -> walletInfo();
                default -> throw JsonRpcError.methodNotFound(request.method());
            };
            return JsonRpcResponse.success(request.id(), result);
        } catch (JsonRpcError e) {
            return JsonRpcResponse.error(request.id(), e.code, e.getMessage(), e.data);
        } catch (Exception e) {
            log.error("JSON-RPC internal error", e);
            return JsonRpcResponse.error(request.id(), JsonRpcError.INTERNAL_ERROR.code,
                    "Internal error", e.getMessage());
        }
    }

    private BlockView mineBlock() {
        Block mined = node.mineNow().block(MINE_TIMEOUT);
        if (mined == null) {
            throw JsonRpcError.internalError("Mining timed out");
        }
        return BlockView.from(mined);
    }

    private Object getBlockByNumber(List<Object> params) {
        if (params.isEmpty()) {
            throw JsonRpcError.invalidParams("Missing block number");
        }
        String selector = String.valueOf(params.get(0));
        boolean fullTx = params.size() < 2 || Boolean.TRUE.equals(params.get(1));

        Block block;
        if ("latest".equalsIgnoreCase(selector)) {
            block = node.latestBlock();
        } else if ("earliest".equalsIgnoreCase(selector)) {
            block = node.blockAtHeight(0);
        } else if ("pending".equalsIgnoreCase(selector)) {
            block = null; // pending blocks are not tracked separately
        } else {
            int height = decodeQuantity(selector).intValue();
            block = node.blockAtHeight(height);
        }
        return block == null ? null : toEthBlock(block, fullTx);
    }

    private Object getBlockByHash(List<Object> params) {
        if (params.isEmpty()) {
            throw JsonRpcError.invalidParams("Missing block hash");
        }
        String hash = String.valueOf(params.get(0));
        boolean fullTx = params.size() < 2 || Boolean.TRUE.equals(params.get(1));
        Block block = node.blockByHash(stripHexPrefix(hash));
        return block == null ? null : toEthBlock(block, fullTx);
    }

    private Map<String, Object> toEthBlock(Block block, boolean fullTx) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("number", encodeQuantity(block.getHeight()));
        map.put("hash", addHexPrefix(block.getHashHex()));
        map.put("parentHash", addHexPrefix(block.getPreviousHashHex()));
        map.put("nonce", encodeQuantity(block.getNonce()));
        map.put("difficulty", encodeQuantity(block.getCompactDifficultyBits()));
        map.put("timestamp", encodeQuantity(block.getTimeMillis() / 1000));
        map.put("miner", addHexPrefix(localAddress()));
        map.put("size", encodeQuantity(block.getTxList().size()));
        map.put("transactions", fullTx
                ? block.getTxList().stream().map(this::toEthTransaction).toList()
                : block.getTxList().stream().map(tx -> addHexPrefix(tx.calcHashHex())).toList());
        map.put("sbBlock", BlockView.from(block));
        return map;
    }

    private Map<String, Object> toEthTransaction(Transaction tx) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("hash", addHexPrefix(tx.calcHashHex()));
        map.put("isCoinbase", tx.isCoinbase());
        map.put("tip", tx.getTip());
        map.put("maxFee", tx.getMaxFee());
        map.put("inputs", tx.getInputs());
        map.put("outputs", tx.getOutputs());
        return map;
    }

    private Object getBalance(List<Object> params) {
        if (params.isEmpty()) {
            throw JsonRpcError.invalidParams("Missing address");
        }
        String address = stripHexPrefix(String.valueOf(params.get(0)));
        Map<String, TxOutput> utxo = node.currentUtxoIncludingPending();
        double balance = utxo.values().stream()
                .filter(out -> out.recipientAddress().equals(address))
                .mapToDouble(TxOutput::value)
                .sum();
        return encodeQuantity(toBaseUnits(balance));
    }

    private Object sendTransaction(List<Object> params) {
        if (params.isEmpty()) {
            throw JsonRpcError.invalidParams("Missing transaction object");
        }
        Object raw = params.get(0);
        if (!(raw instanceof Map<?, ?> txMap)) {
            throw JsonRpcError.invalidParams("Transaction must be an object");
        }
        Object toValue = txMap.get("to");
        Object valueRaw = txMap.get("value");
        if (toValue == null || valueRaw == null) {
            throw JsonRpcError.invalidParams("`to` and `value` are required");
        }
        String recipient = stripHexPrefix(String.valueOf(toValue));
        double amount = toCoins(decodeQuantity(valueRaw));

        Transaction tx = wallet.createTx(recipient, amount, node.currentUtxo());
        if (!node.submitTx(tx)) {
            throw JsonRpcError.internalError("Transaction rejected by mempool");
        }
        return addHexPrefix(tx.calcHashHex());
    }

    private List<BlockView> chainPage(List<Object> params) {
        int page = params.size() > 0 ? toInt(params.get(0)) : 0;
        int size = params.size() > 1 ? toInt(params.get(1)) : 5;
        return node.blockPage(page, size).stream().map(BlockView::from).toList();
    }

    private Map<String, Object> walletInfo() {
        Map<String, TxOutput> confirmed = node.currentUtxo();
        Map<String, TxOutput> effective = node.currentUtxoIncludingPending();
        double confirmedBalance = wallet.balance(confirmed);
        double effectiveBalance = wallet.balance(effective);
        double pendingIncoming = Math.max(0.0, effectiveBalance - confirmedBalance);
        double pendingOutgoing = Math.max(0.0, confirmedBalance - effectiveBalance);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("address", localAddress());
        map.put("confirmedBalance", confirmedBalance);
        map.put("pendingIncoming", pendingIncoming);
        map.put("pendingOutgoing", pendingOutgoing);
        return map;
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return decodeQuantity(value).intValue();
    }

    private BigInteger decodeQuantity(Object value) {
        if (value instanceof Number number) {
            return BigInteger.valueOf(number.longValue());
        }
        String str = String.valueOf(value).trim();
        try {
            if (str.startsWith("0x") || str.startsWith("0X")) {
                return new BigInteger(str.substring(2), 16);
            }
            return new BigInteger(str);
        } catch (NumberFormatException e) {
            throw JsonRpcError.invalidParams("Invalid quantity: " + value);
        }
    }

    private BigInteger toBaseUnits(double coins) {
        return new BigDecimal(coins).multiply(COIN_SCALE).toBigInteger();
    }

    private double toCoins(BigInteger baseUnits) {
        return new BigDecimal(baseUnits).divide(COIN_SCALE).doubleValue();
    }

    private String encodeQuantity(long value) {
        return "0x" + Long.toHexString(value);
    }

    private String encodeQuantity(BigInteger value) {
        return "0x" + value.toString(16);
    }

    private String addHexPrefix(String hex) {
        return hex != null && hex.startsWith("0x") ? hex : "0x" + (hex == null ? "" : hex);
    }

    private String stripHexPrefix(String hex) {
        if (hex == null) {
            return null;
        }
        return hex.startsWith("0x") || hex.startsWith("0X") ? hex.substring(2) : hex;
    }

    private String localAddress() {
        return AddressUtils.publicKeyToAddress(wallet.getLocalWallet().getPublicKey());
    }

    private static class JsonRpcError extends RuntimeException {
        static final JsonRpcError INVALID_REQUEST = new JsonRpcError(-32600, "Invalid request");
        static final JsonRpcError METHOD_NOT_FOUND = new JsonRpcError(-32601, "Method not found");
        static final JsonRpcError INVALID_PARAMS   = new JsonRpcError(-32602, "Invalid params");
        static final JsonRpcError INTERNAL_ERROR   = new JsonRpcError(-32603, "Internal error");

        final int    code;
        final Object data;

        JsonRpcError(int code, String message) {
            super(message);
            this.code = code;
            this.data = null;
        }

        JsonRpcError(int code, String message, Object data) {
            super(message);
            this.code = code;
            this.data = data;
        }

        static JsonRpcError methodNotFound(String method) {
            return new JsonRpcError(METHOD_NOT_FOUND.code,
                    "Method '" + method + "' not found", null);
        }

        static JsonRpcError invalidParams(String msg) {
            return new JsonRpcError(INVALID_PARAMS.code, msg, null);
        }

        static JsonRpcError internalError(String msg) {
            return new JsonRpcError(INTERNAL_ERROR.code, msg, null);
        }
    }
}
