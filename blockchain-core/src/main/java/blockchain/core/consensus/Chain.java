package blockchain.core.consensus;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import blockchain.core.crypto.HashingUtils;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

/** Mutable, thread-safe* blockchain – guarded by concurrent collections. */
public class Chain {

    private final CopyOnWriteArrayList<Block>             blocks = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, TxOutput>     utxo   = new ConcurrentHashMap<>();
    private final Set<String>   coinbaseOutputs  = ConcurrentHashMap.newKeySet();
    private final Map<String,Integer> blockHeightIndex = new ConcurrentHashMap<>();
    private BigInteger cumulativeWork = BigInteger.ZERO;
    private int currentBits           = 0x1f0fffff;

    public Chain() {
        Block g = genesisBlock();
        blocks.add(g);
        blockHeightIndex.put(g.getHashHex(), g.getHeight());
    }

    public Block               getLatest()              { return blocks.get(blocks.size() - 1); }
    public List<Block>         getBlocks()              { return List.copyOf(blocks); }
    public Map<String,TxOutput> getUtxoSnapshot()       { return Map.copyOf(utxo); }
    public BigInteger          getTotalWork()           { return cumulativeWork; }

    public void addBlock(Block b) {
        // 1) Transaction‐level checks first
        validateTxs(b);

        // 2) Then proof‐of‐work
        if (!b.isProofValid()) 
            throw new BlockchainException("invalid PoW");

        // 3) Then previous‐hash linkage
        if (!Objects.equals(b.getPreviousHashHex(), getLatest().getHashHex()))
            throw new BlockchainException("prev-hash mismatch");

        // 4) All good → append & update
        blocks.add(b);
        blockHeightIndex.put(b.getHashHex(), b.getHeight());
        updateUtxo(b);
        maybeAdjustDifficulty();
        cumulativeWork = cumulativeWork.add(workForBits(b.getCompactDifficultyBits()));
    }

    /* ---------- helpers ---------- */

    private Block genesisBlock() {
        Wallet miner  = new Wallet();
        Transaction cb = new Transaction(miner.getPublicKey(), ConsensusParams.blockReward(0));
        Block g = new Block(0, "0".repeat(64), List.of(cb), currentBits,
                            Instant.now().toEpochMilli(), 0);
        updateUtxo(g);
        return g;
    }

    private void validateTxs(Block b) {
        if (b.getTxList().isEmpty()) throw new BlockchainException("empty block");

        Transaction coinbase = b.getTxList().get(0);
        if (!coinbase.isCoinbase()) throw new BlockchainException("first tx must be coinbase");

        // extremely trimmed validation – extend as needed
        b.getTxList().stream().skip(1).forEach(tx -> {
            if (!tx.verifySignatures()) throw new BlockchainException("bad signature");
        });
    }

    private void updateUtxo(Block b) {
        for (Transaction tx : b.getTxList()) {
            int idx = 0;
            for (TxOutput out : tx.getOutputs()) {
                String id = out.id(tx.calcHashHex(), idx++);
                utxo.put(id, out);
                if (tx.isCoinbase()) coinbaseOutputs.add(id);
            }
        }
    }

    private void maybeAdjustDifficulty() {
        int h = blocks.size() - 1;
        if (h == 0 || h % ConsensusParams.DIFFICULTY_WINDOW != 0) return;

        long span  = blocks.get(h).getTimeMillis() -
                     blocks.get(h - ConsensusParams.DIFFICULTY_WINDOW).getTimeMillis();
        long ideal = ConsensusParams.TARGET_BLOCK_INTERVAL_MS * ConsensusParams.DIFFICULTY_WINDOW;

        BigInteger target = HashingUtils.compactToTarget(currentBits);
        if (span < ideal / 2)      target = target.shiftRight(1);
        else if (span > ideal * 2) target = target.shiftLeft(1);

        currentBits = HashingUtils.targetToCompact(target);
    }

    private static BigInteger workForBits(int bits) {
        return BigInteger.ONE.shiftLeft(256)
                             .divide(HashingUtils.compactToTarget(bits));
    }
}
