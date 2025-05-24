package blockchain.core.model;

import java.security.PublicKey;

/**
 * Brand-new UTXO produced by a TX.
 *
 * Java 17 record → zero boiler-plate, fields are final & accessor names match.
 */
public record TxOutput(double value, PublicKey recipient) {

    /** Deterministic id = parentHash : indexInParent. */
    public String id(String parentHash, int idx) {
        return parentHash + ':' + idx;
    }
}
