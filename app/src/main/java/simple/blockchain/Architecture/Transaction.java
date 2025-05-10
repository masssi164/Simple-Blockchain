package simple.blockchain.Architecture;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import simple.blockchain.Utils.CryptoUtil;
import simple.blockchain.Utils.HashUtil;

@Getter
public class Transaction {

    private final List<TxInput> inputs  = new ArrayList<>();
    private final List<TxOutput> outputs = new ArrayList<>();
    private String txHash;

    /* Coinbase */
    public Transaction(PublicKey miner, double reward) {
        outputs.add(new TxOutput(reward, miner));
        txHash = calcHashInternal();
    }

    public Transaction() {}

    public boolean isCoinbase() { return inputs.isEmpty(); }

    /* -------- Signaturen -------- */

    public void signInputs(PrivateKey privKey) {
        if (isCoinbase()) return;
        inputs.forEach(in ->
             in.setSignature(CryptoUtil.applyECDSASig(privKey, in.getReferencedOutputId())));
        txHash = calcHashInternal();
    }
    public boolean verifySignatures() {
        return isCoinbase() || inputs.stream().allMatch(in ->
               CryptoUtil.verifyECDSASig(in.getSender(), in.getReferencedOutputId(), in.getSignature()));
    }

    /* -------- Hash / Merkle -------- */

    public String calcHash() { return txHash != null ? txHash : calcHashInternal(); }

    private String calcHashInternal() {
        StringBuilder sb = new StringBuilder();
        inputs .forEach(i -> sb.append(i.getReferencedOutputId()));
        outputs.forEach(o -> sb.append(o.getRecipient()).append(o.getValue()));
        return HashUtil.sha256(sb.toString());
    }
}
