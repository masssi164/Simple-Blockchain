package blockchain.core.model;

import java.security.PublicKey;

/** Immutable value transfer to <code>recipient</code>. */
public record TxOutput(double value, PublicKey recipient) {

    /** Canonical identifier – “&lt;parentHash&gt;:&lt;index&gt;”. */
    public String id(String parentHash, int index) {
        return parentHash + ':' + index;
    }
}
