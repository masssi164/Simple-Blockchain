package blockchain.core.model;

import blockchain.core.crypto.AddressUtils;

import java.security.PublicKey;

public record TxOutput(double value, String recipientAddress) {

    /** Helper so tests (and older code) that still pass a PublicKey keep compiling */
    public TxOutput(double value, PublicKey recipient) {
        this(value, AddressUtils.publicKeyToAddress(recipient));
    }

    public String id(String parentHash, int index) {
        return parentHash + ':' + index;
    }
}
