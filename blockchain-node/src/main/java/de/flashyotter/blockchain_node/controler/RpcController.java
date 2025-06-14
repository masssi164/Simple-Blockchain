package de.flashyotter.blockchain_node.controler;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Block;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.dto.RpcRequest;
import de.flashyotter.blockchain_node.dto.RpcResponse;
import de.flashyotter.blockchain_node.dto.RpcResponse.RpcError;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Simple JSON-RPC 2.0 facade for programmatic access. */
@RestController
@RequestMapping("/api/rpc")
@RequiredArgsConstructor
public class RpcController {

    private final NodeService  node;
    private final WalletService wallet;

    @PostMapping
    public RpcResponse handle(@RequestBody RpcRequest req) {
        try {
            return switch (req.method()) {
                case "chain_latestBlock" ->
                    new RpcResponse("2.0", req.id(), node.latestBlock(), null);
                case "node_mine" -> {
                    Block b = node.mineNow().block();
                    yield new RpcResponse("2.0", req.id(), b, null);
                }
                case "wallet_info" -> walletInfo(req.id());
                default -> new RpcResponse("2.0", req.id(), null,
                        new RpcError(-32601, "Method not found"));
            };
        } catch (Exception e) {
            return new RpcResponse("2.0", req.id(), null,
                    new RpcError(-32000, e.getMessage()));
        }
    }

    private RpcResponse walletInfo(String id) {
        Map<String, TxOutput> utxo = node.currentUtxoIncludingPending();
        double balance = wallet.balance(utxo);
        String address = AddressUtils.publicKeyToAddress(
                wallet.getLocalWallet().getPublicKey());
        Map<String, Object> info = Map.of(
                "address", address,
                "balance", balance);
        return new RpcResponse("2.0", id, info, null);
    }
}
