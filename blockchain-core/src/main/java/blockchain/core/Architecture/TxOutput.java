package blockchain.core.Architecture;

import java.security.PublicKey;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TxOutput {
    private double     value;
    private PublicKey  recipient;
    public String id(String parentHash, int idx) { return parentHash + ":" + idx; }
}
