package blockchain.core.model;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

import blockchain.core.crypto.HashingUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A full block = immutable header + transaction list.
 * Mining / PoW live entirely in the header.
 */
@Getter @Slf4j
public class Block {

    private final BlockHeader       header;
    private final List<Transaction> txList;

    /* ── public ctor used by code & tests ───────────────────────────── */
    public Block(int height,
                 String prevHashHex,
                 List<Transaction> txs,
                 int compactBits) {
        this(height, prevHashHex, txs, compactBits,
             Instant.now().toEpochMilli(), 0);
    }

    /* ── extended ctor (genesis, determin. tests) ───────────────────── */
    public Block(int  height,
                 String prevHashHex,
                 List<Transaction> txs,
                 int  compactBits,
                 long fixedTimeMillis,
                 int  fixedNonce) {

        String merkle = HashingUtils.computeMerkleRoot(
                txs.stream().map(Transaction::calcHashHex).toList());

        this.header = new BlockHeader(height, prevHashHex, merkle,
                                      compactBits, fixedTimeMillis, fixedNonce);
        this.txList = List.copyOf(txs);
    }

    /* mining helpers  */
    public void mineLocally() {
        BigInteger target = HashingUtils.compactToTarget(header.compactDifficultyBits);

        while (true) {
            if (header.isProofValid()) {
                log.info("⛏️  Block {} mined → {}", header.height, header.getHashHex());
                return;
            }
            header.incrementNonce();              // bump & re-hash
        }
    }

    public boolean isProofValid() { return header.isProofValid(); }

    /* ── delegates keep external API unchanged ─────────────────────── */
    public int    getHeight()                { return header.height; }
    public String getPreviousHashHex()       { return header.previousHashHex; }
    public long   getTimeMillis()            { return header.timeMillis; }
    public int    getCompactDifficultyBits() { return header.compactDifficultyBits; }
    public String getHashHex()               { return header.getHashHex(); }
    public int    getNonce()                 { return header.getNonce(); }
    public String getMerkleRootHex()         { return header.merkleRootHex; }
}
