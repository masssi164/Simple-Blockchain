package de.flashyotter.blockchain_node.controler;

import blockchain.core.crypto.CryptoUtils;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.dto.SendFundsDto;
import de.flashyotter.blockchain_node.dto.WalletInfoDto;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService wallet;
    private final NodeService   node;

    /* ─── info ──────────────────────────────────────────────────── */
    @GetMapping("/info")
    public WalletInfoDto info() {
        double bal = wallet.balance(node.currentUtxo());
        return new WalletInfoDto(
                CryptoUtils.keyToBase64(wallet.getLocalWallet().getPublicKey()),
                bal);
    }

    /* ─── send ──────────────────────────────────────────────────── */
    @PostMapping("/send")
    public Transaction send(@RequestBody SendFundsDto dto) {
        Transaction tx = wallet.createTx(dto.recipient(),
                                         dto.amount(),
                                         node.currentUtxo());
        node.submitTx(tx);          // broadcast + mem-pool
        return tx;
    }
}
