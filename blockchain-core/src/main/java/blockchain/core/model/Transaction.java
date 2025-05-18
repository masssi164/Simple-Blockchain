package blockchain.core.model;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import blockchain.core.crypto.*;
import lombok.Getter;

/**
 * Immutable TX.
 * – coinbaseTx = new Transaction(minerPubKey, reward)
 */
@Getter
public class Transaction {

    private final List<TxInput>  inputs  = new ArrayList<>();
    private final List<TxOutput> outputs = new ArrayList<>();
    private String txHashHex;

    /* ---- Coinbase ---- */
    public Transaction(PublicKey miner, double reward) {
        outputs.add(new TxOutput(reward, miner));
        txHashHex = computeTxHashHex();
    }

    public Transaction() { /* ordinary TX will be populated afterwards */ }

    public boolean isCoinbase() { return inputs.isEmpty(); }

    /* ---- Signatures ---- */

    public void signInputs(PrivateKey privKey) {
        if (isCoinbase()) return;
        inputs.forEach(in ->
                in.setSignature(CryptoUtils.applyEcdsaSignature(privKey, in.getReferencedOutputId())));
        txHashHex = computeTxHashHex();
    }

    public boolean verifySignatures() {
        return isCoinbase() || inputs.stream().allMatch(in ->
                CryptoUtils.verifyEcdsaSignature(in.getSender(),
                        in.getReferencedOutputId(), in.getSignature()));
    }

    /* ---- Hash ---- */

    public String calcHashHex() { return txHashHex != null ? txHashHex : computeTxHashHex(); }

    private String computeTxHashHex() {
        StringBuilder sb = new StringBuilder();
        inputs .forEach(i -> sb.append(i.getReferencedOutputId()));
        outputs.forEach(o -> sb.append(o.getRecipient()).append(o.getValue()));
        return HashingUtils.computeSha256Hex(sb.toString());
    }
}
