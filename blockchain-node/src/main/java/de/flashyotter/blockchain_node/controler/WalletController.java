package de.flashyotter.blockchain_node.controler;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.dto.SendFundsDto;
import de.flashyotter.blockchain_node.dto.WalletInfoDto;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for wallet operations.
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService wallet;
    private final NodeService   node;

    @PostMapping("/send")
    public Transaction send(@RequestBody SendFundsDto dto) {
        // Create a transaction consuming UTXOs and producing outputs (including change)
        Transaction tx = wallet.createTx(
            dto.recipient(),
            dto.amount(),
            node.currentUtxo()
        );
        // Submit to mempool and broadcast
        node.submitTx(tx);
        return tx;
    }

    @GetMapping
    public WalletInfoDto info() {
        // Use the UTXO set that includes pending spends so the balance
        // immediately reflects any unconfirmed outgoing transactions.
        Map<String, TxOutput> utxo = node.currentUtxoIncludingPending();
        double balance = wallet.balance(utxo);
        String address = AddressUtils.publicKeyToAddress(
            wallet.getLocalWallet().getPublicKey()
        );
        return new WalletInfoDto(address, balance);
    }
}
