package simple.blockchain.architecture;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
/**
 * Represents a block in the blockchain.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Block {
    private int index;
    private long timeStamp;
    private String prevHash;
    private String hash;
    private int nonce;
}
