package blockchain.core.model;

import blockchain.core.crypto.CryptoUtils;
import blockchain.core.crypto.HashingUtils;
import lombok.Getter;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable transaction TX.
 *
 * • Coinbase – created with {@link #Transaction(PublicKey, double)}  
 * • Ordinary  – create empty, then populate inputs/outputs, sign.
 */
@Getter
public class Transaction {

    private final List<TxInput>  inputs  = new ArrayList<>();
    private final List<TxOutput> outputs = new ArrayList<>();
    private       String        txHashHex;          // lazy-calculated

    
    /** Coinbase TX paying {@code reward} to {@code miner}. */
    public Transaction(PublicKey miner, double reward) {
        outputs.add(new TxOutput(reward, miner));
        txHashHex = computeTxHashHex();
    }

    /** Ordinary TX – populate IO lists, then {@link #signInputs}. */
    public Transaction() { }

    public boolean isCoinbase() { return inputs.isEmpty(); }

    
    /**
     * Signs every input with {@code privKey}.  
     * Skips coinbase since it has no inputs.
     */
    public void signInputs(PrivateKey privKey) {
        if (isCoinbase()) return;

        inputs.forEach(in ->
                in.setSignature(
                        CryptoUtils.applyEcdsaSignature(privKey, in.getReferencedOutputId())));
        txHashHex = computeTxHashHex();     // new sig → new hash
    }

    /** Verifies all input signatures (always true for coinbase). */
    public boolean verifySignatures() {
        return isCoinbase() || inputs.stream().allMatch(in ->
                CryptoUtils.verifyEcdsaSignature(
                        in.getSender(), in.getReferencedOutputId(), in.getSignature()));
    }

    /* hashing  */

    /** Returns the cached hash or recomputes if not yet set. */
    public String calcHashHex() {
        return txHashHex != null ? txHashHex : computeTxHashHex();
    }

    /** Deterministic TX hash = SHA-256(inputs ∥ outputs). */
    private String computeTxHashHex() {
        StringBuilder sb = new StringBuilder();
        inputs .forEach(i -> sb.append(i.getReferencedOutputId()));
        outputs.forEach(o -> sb.append(o.recipient()).append(o.value()));
        return HashingUtils.computeSha256Hex(sb.toString());
    }
}
