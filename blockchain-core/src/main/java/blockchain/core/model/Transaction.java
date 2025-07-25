package blockchain.core.model;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import blockchain.core.crypto.CryptoUtils;
import blockchain.core.crypto.HashingUtils;

/** Mutable until signatures are applied, then effectively frozen. */
public class Transaction implements java.io.Serializable {

    private final List<TxInput>  inputs  = new ArrayList<>();
    private final List<TxOutput> outputs = new ArrayList<>();
    private String  txHashHex;                     // lazily computed
    private String  coinbaseNonce;                 // for deterministic hashing
    private double  maxFee;                        // sender max willingness
    private double  tip;                           // miner incentive

    /* ------------------------------------------------------------------ */
    /* ctors                                                              */
    /* ------------------------------------------------------------------ */

    /** Empty ctor used by wallet / mem-pool. */
    public Transaction() { }


    /* ------------------------------------------------------------------ */
    /* getters                                                            */
    /* ------------------------------------------------------------------ */

    public List<TxInput>  getInputs()  { return inputs; }
    public List<TxOutput> getOutputs() { return outputs; }
    public boolean isCoinbase()        { return inputs.isEmpty(); }
    public double getMaxFee()          { return maxFee; }
    public void   setMaxFee(double v)  { this.maxFee = v; }
    public double getTip()             { return tip; }
    public void   setTip(double v)     { this.tip = v; }

    /* ------------------------------------------------------------------ */
    /* signing / verification                                             */
    /* ------------------------------------------------------------------ */

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

    /* ------------------------------------------------------------------ */
    /* hashing                                                            */
    /* ------------------------------------------------------------------ */

    public String calcHashHex() {
        if (txHashHex == null) txHashHex = computeTxHashHex();
        return txHashHex;
    }

    // coin-base convenience ctor
    public Transaction(PublicKey recipient, double reward) {
        this(recipient, reward, null);
    }

    /** Coinbase constructor allowing an explicit nonce/height value. */
    public Transaction(PublicKey recipient, double reward, String coinbaseNonce) {
        outputs.add(new TxOutput(reward, recipient));
        this.coinbaseNonce = coinbaseNonce;
        // no inputs
        this.maxFee = 0.0;
        this.tip    = 0.0;
    }

/* -------- internal -------- */
private String computeTxHashHex() {
    StringBuilder sb = new StringBuilder();
    inputs.forEach(i -> sb.append(i.getReferencedOutputId()).append('|'));
    // NOTE: recipientAddress(), not recipient()
    outputs.forEach(o -> sb.append(o.recipientAddress())
                           .append(':')
                           .append(o.value())
                           .append('|'));
    if (isCoinbase() && coinbaseNonce != null) sb.append(coinbaseNonce);
    return HashingUtils.computeSha256Hex(sb.toString());
}

}
