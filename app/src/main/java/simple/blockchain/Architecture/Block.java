package simple.blockchain.Architecture;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import simple.blockchain.Utils.HashUtil;

@Data
@Slf4j
public class Block {

    private final int               index;
    private final long              timeStamp;
    private final String            prevHash;
    private final List<Transaction> transactions;
    private final int               bits;

    private final String merkleRoot;
    private int    nonce = 0;
    private String hash;

    public Block(int index, String prevHash, List<Transaction> tx, int bits) {
        this.index       = index;
        this.timeStamp   = Instant.now().toEpochMilli();
        this.prevHash    = prevHash;
        this.transactions = tx;
        this.bits        = bits;
        this.merkleRoot  = HashUtil.merkleRoot(
                             tx.stream().map(Transaction::calcHash).collect(Collectors.toList()));
        this.hash        = calculateHash();
    }

    private String calculateHash() {
        return HashUtil.sha256(index + prevHash + timeStamp + nonce + merkleRoot);
    }

    public void mine() {
        final BigInteger target = HashUtil.compactToBigInt(bits);
        while (true) {
            byte[] hBytes = HashUtil.sha256Bytes(index + prevHash + timeStamp + nonce + merkleRoot);
            if (new BigInteger(1, hBytes).compareTo(target) <= 0) {
                hash = HashUtil.bytes2Hex(hBytes);
                log.info("Block {} mined: {}", index, hash);
                return;
            }
            nonce++;
        }
    }

    @Override
    public String toString() {
        return "Block{idx=" + index + ", bits=0x" + Integer.toHexString(bits) +
               ", hash=" + hash.substring(0,8) + "...}";
    }
}
