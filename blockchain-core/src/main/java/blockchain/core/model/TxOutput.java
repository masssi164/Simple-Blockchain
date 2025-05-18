package blockchain.core.model;

import java.security.PublicKey;

import lombok.AllArgsConstructor;
import lombok.Data;

/** New UTXO created by the TX: value + recipient PK */
@Data 
@AllArgsConstructor
public class TxOutput {
    private double    value;
    private PublicKey recipient;
    public String id(String parentHash, int idx) {

        return parentHash + ":" + idx;
    }
}
