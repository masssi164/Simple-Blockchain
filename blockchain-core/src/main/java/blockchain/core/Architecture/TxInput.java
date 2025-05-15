package blockchain.core.Architecture;

import java.security.PublicKey;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TxInput {
    private String   referencedOutputId;
    private byte[]   signature;
    private PublicKey sender;
}
