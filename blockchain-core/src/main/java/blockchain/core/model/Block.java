package blockchain.core.model;

import blockchain.core.crypto.HashingUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Immutable block B.
 *
 * Fields<br>
 * • height h (0-based) • timeMillis t • prevHash p • txList T • difficulty bits n<br>
 *
 * Block hash H = SHA-256(h ∥ p ∥ t ∥ nonce ∥ merkleRoot).  
 * PoW is valid iff the interpreted integer hᵢ ≤ target, where target ← bits.
 */
@Data @Slf4j
public class Block {

    /* ─────────── header fields (immutable after construction) ────────── */
    private final int      height;
    private final long     timeMillis;              // unix epoch ms
    private final String   previousHashHex;
    private final List<Transaction> txList;
    private final int      compactDifficultyBits;

    /* ─────────── derived / mutable fields ────────── */
    private final String merkleRootHex;
    private       int    nonce   = 0;
    private       String hashHex;                   // set by ctor or mineLocally()

    /* ===================================================================
     *  standard constructor – time = now, nonce starts at 0
     * =================================================================== */
    public Block(int height,
                 String prevHash,
                 List<Transaction> tx,
                 int bits) {
        this(height, prevHash, tx, bits,
             Instant.now().toEpochMilli(),          // time
             0);                                    // nonce
    }

    /* ===================================================================
     *  extended constructor – allows deterministic genesis creation
     * =================================================================== */
    public Block(int height,
                 String prevHash,
                 List<Transaction> tx,
                 int bits,
                 long fixedTimeMillis,
                 int  fixedNonce) {

        this.height                = height;
        this.timeMillis            = fixedTimeMillis;
        this.previousHashHex       = prevHash;
        this.txList                = List.copyOf(tx);
        this.compactDifficultyBits = bits;

        this.merkleRootHex = HashingUtils.computeMerkleRoot(
                tx.stream().map(Transaction::calcHashHex).collect(Collectors.toList()));

        this.nonce   = fixedNonce;
        this.hashHex = computeBlockHashHex();
    }

    /* ─────────── internal helpers ────────── */
    private String computeBlockHashHex() {
        return HashingUtils.computeSha256Hex(
                height + previousHashHex + timeMillis + nonce + merkleRootHex);
    }

    /* ===================================================================
     *  mining – brute-force nonce until hash ≤ target
     * =================================================================== */
    public void mineLocally() {
        final BigInteger target = HashingUtils.compactToTarget(compactDifficultyBits);

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

    /** Re-evaluates PoW for a received (foreign) block. */
    public boolean isProofValid() {
        BigInteger target = HashingUtils.compactToTarget(compactDifficultyBits);
        BigInteger val    = new BigInteger(1,
                HashingUtils.computeSha256Bytes(height + previousHashHex +
                                                timeMillis + nonce + merkleRootHex));
        return val.compareTo(target) <= 0;   // pass if hᵢ ≤ T
    }
}
