package blockchain.core.model;

import java.security.PublicKey;

import lombok.AllArgsConstructor;
import lombok.Data;

/** New UTXO created by the TX: value + recipient PK */
@Data 
@AllArgsConstructor
public class TxInput {
    private String   referencedOutputId;
    private byte[]   signature;
    private PublicKey sender;
}
