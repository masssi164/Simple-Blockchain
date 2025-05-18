package blockchain.core.consensus;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import blockchain.core.crypto.HashingUtils;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

/** Very small PoW chain – genesis + append + difficulty retarget. */
public class Chain {

    private static final long TARGET_MS  = 60_000;
    private static final int  ADJUST_INT = 10;

    private int currentBits = 0x1f0fffff;
    private final List<Block> blocks = new ArrayList<>();
    private final Map<String, TxOutput> utxo = new HashMap<>();

    public Chain() {
        blocks.add(genesisBlock());
    }

    /* ---------- public API ---------- */

    public synchronized Block getLatest() { return blocks.get(blocks.size() - 1); }

    public synchronized List<Block> getBlocks() { return List.copyOf(blocks); }

    public synchronized void addBlock(Block b) {
        if (!b.isProofValid()) throw new BlockchainException("invalid PoW");
        if (!Objects.equals(b.getPreviousHashHex(), getLatest().getHashHex()))
            throw new BlockchainException("prev-hash mismatch");
        validateTxs(b.getTxList());
        blocks.add(b);
        updateUtxo(b);
        maybeAdjustDifficulty();
    }

    /* ---------- helpers ---------- */

    private Block genesisBlock() {
        Wallet w = new Wallet();
        Transaction coinbase = new Transaction(w.getPublicKey(), 50);
        Block g = new Block(0, "0".repeat(64), List.of(coinbase), currentBits);
        updateUtxo(g);
        return g;
    }

    private void validateTxs(List<Transaction> txs) {
        for (Transaction tx : txs)
            if (!tx.verifySignatures()) throw new BlockchainException("bad TX sig");
    }

    private void updateUtxo(Block b) {
        for (Transaction tx : b.getTxList()) {
            tx.getInputs().forEach(in -> utxo.remove(in.getReferencedOutputId()));
            int idx = 0;
            for (TxOutput out : tx.getOutputs())
                utxo.put(out.id(tx.calcHashHex(), idx++), out);
        }
    }

    private void maybeAdjustDifficulty() {
        int h = blocks.size() - 1;
        if (h == 0 || h % ADJUST_INT != 0) return;

        long actualSpan = blocks.get(h).getTimeMillis() - blocks.get(h - ADJUST_INT).getTimeMillis();
        long idealSpan  = TARGET_MS * ADJUST_INT;

        BigInteger target = HashingUtils.compactToTarget(currentBits);
        if (actualSpan < idealSpan / 2)      target = target.shiftRight(1);
        else if (actualSpan > idealSpan * 2) target = target.shiftLeft(1);

        currentBits = HashingUtils.targetToCompact(target);
    }
}
