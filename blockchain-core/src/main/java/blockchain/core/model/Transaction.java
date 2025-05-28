package blockchain.core.model;

import blockchain.core.crypto.CryptoUtils;
import blockchain.core.crypto.HashingUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/** Mutable until signatures are applied, then effectively frozen. */
public class Transaction {

    private final List<TxInput>  inputs  = new ArrayList<>();
    private final List<TxOutput> outputs = new ArrayList<>();
    private String txHashHex;                      // lazily computed

    /* --------- constructors --------- */

    /** Empty ctor used by wallet / mem-pool. */
    public Transaction() { }

    /** Convenience coin-base constructor used in genesis & mining. */
    public Transaction(PublicKey recipient, double reward) {
        outputs.add(new TxOutput(reward, recipient));
        // coin-base has no inputs
    }

    /* --------- getters --------- */

    public List<TxInput>  getInputs()  { return inputs; }
    public List<TxOutput> getOutputs() { return outputs; }
    public boolean isCoinbase()        { return inputs.isEmpty(); }

    /* --------- signing / verification --------- */

    public void signInputs(PrivateKey privKey) {
        if (isCoinbase()) return;
        inputs.forEach(in -> in.setSignature(
                CryptoUtils.applyEcdsaSignature(privKey, in.getReferencedOutputId())));
        txHashHex = null;     // force re-hash after signatures
    }

    public boolean verifySignatures() {
        if (isCoinbase()) return true;
        return inputs.stream().allMatch(in ->
                CryptoUtils.verifyEcdsaSignature(in.getSender(),
                                                 in.getReferencedOutputId(),
                                                 in.getSignature()));
    }

    /* --------- hashing --------- */

    public String calcHashHex() {
        if (txHashHex == null) txHashHex = computeTxHashHex();
        return txHashHex;
    }

    private String computeTxHashHex() {
        StringBuilder sb = new StringBuilder();
        inputs .forEach(i -> sb.append(i.getReferencedOutputId()));
        outputs.forEach(o -> sb.append(o.recipient()).append(o.value()));
        return HashingUtils.computeSha256Hex(sb.toString());
    }
}
