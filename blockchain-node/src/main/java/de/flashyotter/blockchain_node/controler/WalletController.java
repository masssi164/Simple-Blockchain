package de.flashyotter.blockchain_node.controler;

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

    /** Send funds from this node’s wallet. */
    @PostMapping("/send")
    public Transaction send(@RequestBody SendFundsDto dto) {
        Transaction tx = wallet.createTx(dto.recipient(),
                                         dto.amount(),
                                         node.currentUtxo());
        node.submitTx(tx);
        return tx;
    }

    /** Simple balance / address endpoint for the UI. */
    @GetMapping
    public WalletInfoDto info() {
        double bal = wallet.balance(node.currentUtxo());
        return new WalletInfoDto(wallet.getLocalWallet()
                                       .getPublicKey()
                                       .getEncoded()
                                       .toString(),  // base64 in UI layer
                                 bal);
    }
}
