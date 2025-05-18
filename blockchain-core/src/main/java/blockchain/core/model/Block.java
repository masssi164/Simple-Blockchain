package blockchain.core.model;

import blockchain.core.crypto.HashingUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Immutable block.
 * PoW target = compactDifficultyBits → realTarget (see HashingUtils).
 */
@Data
@Slf4j
public class Block {

    private final int    height;            // zero-based index
    private final long   timeMillis;        // unix epoch ms
    private final String previousHashHex;
    private final List<Transaction> txList;
    private final int    compactDifficultyBits;

    /* derived */
    private final String merkleRootHex;
    private int          nonce = 0;
    private String       hashHex;           // set after mine() or verify()

    public Block(int height, String prevHash, List<Transaction> tx, int bits) {
        this.height                 = height;
        this.timeMillis             = Instant.now().toEpochMilli();
        this.previousHashHex        = prevHash;
        this.txList                 = tx;
        this.compactDifficultyBits  = bits;
        this.merkleRootHex          = HashingUtils.computeMerkleRoot(
                                          tx.stream().map(Transaction::calcHashHex)
                                            .collect(Collectors.toList()));
        this.hashHex = computeBlockHashHex();
    }

    /** H(height∥prev∥time∥nonce∥merkleRoot). */
    private String computeBlockHashHex() {
        return HashingUtils.computeSha256Hex(
                height + previousHashHex + timeMillis + nonce + merkleRootHex);
    }

    /** Proof-of-work loop – exits when hash ≤ target. */
    public void mineLocally() {
        BigInteger target = HashingUtils.compactToTarget(compactDifficultyBits);
        while (true) {
            byte[] hBytes = HashingUtils.computeSha256Bytes(
                    height + previousHashHex + timeMillis + nonce + merkleRootHex);
            if (new BigInteger(1, hBytes).compareTo(target) <= 0) {
                hashHex = HashingUtils.bytesToHex(hBytes);
                log.info("⛏️  Block {} mined → {}", height, hashHex);
                return;
            }
            nonce++;
        }
    }

    /** Verifies a foreign block’s PoW. */
    public boolean isProofValid() {
        BigInteger target = HashingUtils.compactToTarget(compactDifficultyBits);
        BigInteger val    = new BigInteger(1,
                HashingUtils.computeSha256Bytes(height + previousHashHex +
                                                timeMillis + nonce + merkleRootHex));
        return val.compareTo(target) <= 0;
    }
}
