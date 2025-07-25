package blockchain.core.model;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import blockchain.core.crypto.HashingUtils;

/**
 * Immutable header that is actually covered by Proof-of-Work.
 * <p>
 * Hash = SHA-256(height ‖ prevHash ‖ time ‖ nonce ‖ merkleRoot)
 */
public final class BlockHeader implements java.io.Serializable {

    /* consensus-critical fields  */
    public final int    height;
    public final String previousHashHex;
    public final long   timeMillis;
    public final String merkleRootHex;
    public final int    compactDifficultyBits;

    private int    nonce;
    private String hashHex;

    /* ================================================================= */
    /*  ctors                                                            */
    /* ================================================================= */
    public BlockHeader(int height,
                       String prevHashHex,
                       String merkleRootHex,
                       int compactBits) {
        this(height, prevHashHex, merkleRootHex,
             compactBits, Instant.now().toEpochMilli(), 0);
    }

    @JsonCreator
    public BlockHeader(@JsonProperty("height") int height,
                       @JsonProperty("previousHashHex") String prevHashHex,
                       @JsonProperty("merkleRootHex") String merkleRootHex,
                       @JsonProperty("compactDifficultyBits") int compactBits,
                       @JsonProperty("timeMillis") long fixedTimeMillis,
                       @JsonProperty("nonce") int  fixedNonce) {
        this.height                = height;
        this.previousHashHex       = prevHashHex;
        this.merkleRootHex         = merkleRootHex;
        this.compactDifficultyBits = compactBits;
        this.timeMillis            = fixedTimeMillis;
        this.nonce                 = fixedNonce;

        this.hashHex = computeHash();
    }

    /* ================================================================= */
    /*  mining helpers                                                   */
    /* ================================================================= */
    public void incrementNonce() {
        nonce++;
        hashHex = computeHash();
    }

    public boolean isProofValid() {
        BigInteger target = HashingUtils.compactToTarget(compactDifficultyBits);
        BigInteger val    = new BigInteger(1, HashingUtils.computeSha256Bytes(hashPreimage()));
        return val.compareTo(target) <= 0;
    }

    /* ================================================================= */
    /*  accessors                                                        */
    /* ================================================================= */
    public int    getNonce()   { return nonce; }
    public String getHashHex() { return hashHex; }

    /* ================================================================= */
    /*  internal                                                         */
    /* ================================================================= */
    private String computeHash() {
        return HashingUtils.computeSha256Hex(hashPreimage());
    }

    private String hashPreimage() {
        return height + previousHashHex + timeMillis + nonce + merkleRootHex;
    }

    @Override
    public int hashCode() { return Objects.hash(hashHex); }

    @Override
    public boolean equals(Object o) {
        return (o instanceof BlockHeader h) && Objects.equals(hashHex, h.hashHex);
    }
}
