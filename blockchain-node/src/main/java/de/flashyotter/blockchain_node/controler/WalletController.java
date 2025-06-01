package de.flashyotter.blockchain_node.controler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.dto.SendFundsDto;
import de.flashyotter.blockchain_node.dto.WalletInfoDto;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService wallet;
    private final NodeService   node;

    @PostMapping("/send")
    public Transaction send(@RequestBody SendFundsDto dto) {
        Transaction tx = wallet.createTx(dto.recipient(), dto.amount(),
                                         node.currentUtxo());
        node.submitTx(tx);
        return tx;
    }

    @GetMapping
    public WalletInfoDto info() {
        double  bal   = wallet.balance(node.currentUtxo());
        String  addr  = AddressUtils.publicKeyToAddress(
                            wallet.getLocalWallet().getPublicKey());
        return new WalletInfoDto(addr, bal);
    }
}
