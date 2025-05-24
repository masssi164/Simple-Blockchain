package blockchain.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.PublicKey;

/**
 * References an existing UTXO to be spent.
 */
@Data @AllArgsConstructor
public class TxInput {
    private String    referencedOutputId;   // UTXO id being spent
    private byte[]    signature;            // ECDSA over referencedOutputId
    private PublicKey sender;               // owner of the UTXO
}
