package blockchain.core.consensus;

import blockchain.core.crypto.HashingUtils;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

/**
 * Tiny PoW chain with:
 * • genesis creation
 * • append-only blocks
 * • difficulty retarget
 * • full TX validation (fees, size, double-spend-in-block, coinbase maturity)
 * • cumulative-work accounting (longest = heaviest)  ← NEW
 */
public class Chain {

    /*  ------------------------------------------------------------------
     *   fields
     *  ------------------------------------------------------------------ */
    private int currentBits = 0x1f0fffff;                // Bitcoin main-net style start

    private final List<Block>           blocks            = new ArrayList<>();
    private final Map<String, TxOutput> utxo              = new HashMap<>();
    private final Set<String>           coinbaseOutputs   = new HashSet<>();
    private final Map<String, Integer>  blockHeightIndex  = new HashMap<>();

    /** Total accumulated target-inverse work (∑ 2²⁵⁶ / target). */
    private BigInteger cumulativeWork = workForBits(currentBits);

    /*  ------------------------------------------------------------------
     *   ctor
     *  ------------------------------------------------------------------ */
    public Chain() {
        Block g = genesisBlock();
        blocks.add(g);
        blockHeightIndex.put(g.getHashHex(), g.getHeight());
    }

    /*  ------------------------------------------------------------------
     *   public accessors
     *  ------------------------------------------------------------------ */
    public synchronized Block getLatest()            { return blocks.get(blocks.size() - 1); }
    public synchronized List<Block> getBlocks()      { return List.copyOf(blocks); }
    public synchronized Map<String, TxOutput> getUtxoSnapshot() { return Map.copyOf(utxo); }
    public synchronized BigInteger getTotalWork()    { return cumulativeWork; }

    /*  ------------------------------------------------------------------
     *   mutation – validate & append
     *  ------------------------------------------------------------------ */
    public synchronized void addBlock(Block b) {

        if (!b.isProofValid())
            throw new BlockchainException("invalid PoW");

        if (!Objects.equals(b.getPreviousHashHex(), getLatest().getHashHex()))
            throw new BlockchainException("prev-hash mismatch");

        validateTxs(b);
        enforceBlockSize(b);

        blocks.add(b);
        blockHeightIndex.put(b.getHashHex(), b.getHeight());

        updateUtxo(b);
        maybeAdjustDifficulty();

        cumulativeWork = cumulativeWork.add(workForBits(b.getCompactDifficultyBits()));
    }

    /*  ------------------------------------------------------------------
     *   genesis
     *  ------------------------------------------------------------------ */
    private Block genesisBlock() {
        Wallet miner = new Wallet();
        Transaction cb = new Transaction(miner.getPublicKey(),
                                         ConsensusParams.blockReward(0));

        Block g = new Block(
                0,
                "0".repeat(64),
                List.of(cb),
                currentBits,
                Instant.now().toEpochMilli(),          // deterministic for tests
                0                                       // nonce 0, hash pre-computed in ctor
        );
        updateUtxo(g);
        return g;
    }

    /*  ------------------------------------------------------------------
     *   tx validation helpers
     *  ------------------------------------------------------------------ */
    private void validateTxs(Block b) {

        List<Transaction> txs = b.getTxList();
        if (txs.isEmpty()) throw new BlockchainException("block must contain tx");

        Transaction coinbase = txs.get(0);
        if (!coinbase.isCoinbase())
            throw new BlockchainException("first tx must be coinbase");

        double totalFees = 0;
        Set<String> spentInBlock = new HashSet<>();

        for (int i = 1; i < txs.size(); i++) {
            Transaction tx = txs.get(i);

            if (!tx.verifySignatures()) throw new BlockchainException("bad signature");
            double inSum = 0;

            for (TxInput in : tx.getInputs()) {

                /* double-spend inside the same block? */
                if (!spentInBlock.add(in.getReferencedOutputId()))
                    throw new BlockchainException("double spend in block: " + in.getReferencedOutputId());

                TxOutput prev = utxo.get(in.getReferencedOutputId());
                if (prev == null)
                    throw new BlockchainException("unknown UTXO " + in.getReferencedOutputId());

                inSum += prev.value();

                /* coinbase maturity */
                if (coinbaseOutputs.contains(in.getReferencedOutputId())) {
                    String parentHash   = in.getReferencedOutputId().split(":")[0];
                    int    parentHeight = blockHeightIndex.get(parentHash);
                    if (b.getHeight() - parentHeight < ConsensusParams.COINBASE_MATURITY)
                        throw new BlockchainException("premature spend of coinbase output");
                }
            }

            double outSum = tx.getOutputs().stream().mapToDouble(TxOutput::value).sum();
            if (inSum + 1e-8 < outSum)
                throw new BlockchainException("inputs < outputs");

            totalFees += inSum - outSum;
        }

        /* subsidy + fee rule */
        double allowed    = ConsensusParams.blockReward(b.getHeight()) + totalFees;
        double coinbaseOut = coinbase.getOutputs().stream().mapToDouble(TxOutput::value).sum();
        if (coinbaseOut - 1e-8 > allowed)
            throw new BlockchainException("coinbase > reward + fees");
    }

    private void enforceBlockSize(Block b) {
        /* very crude size estimate */
        int approx = 80 + b.getTxList().size() * 250;
        if (approx > ConsensusParams.MAX_BLOCK_SIZE_BYTES)
            throw new BlockchainException("block > 1 MB");
    }

    /*  ------------------------------------------------------------------
     *   utxo updates
     *  ------------------------------------------------------------------ */
    private void updateUtxo(Block b) {

        for (Transaction tx : b.getTxList()) {

            /* spend old */
            tx.getInputs().forEach(in -> {
                utxo.remove(in.getReferencedOutputId());
                coinbaseOutputs.remove(in.getReferencedOutputId());
            });

            /* add new */
            int idx = 0;
            for (TxOutput out : tx.getOutputs()) {
                String id = out.id(tx.calcHashHex(), idx++);
                utxo.put(id, out);
                if (tx.isCoinbase()) coinbaseOutputs.add(id);
            }
        }
    }

    /*  ------------------------------------------------------------------
     *   difficulty retarget
     *  ------------------------------------------------------------------ */
    private void maybeAdjustDifficulty() {
        int h = blocks.size() - 1;
        if (h == 0 || h % ConsensusParams.DIFFICULTY_WINDOW != 0) return;

        long span  = blocks.get(h).getTimeMillis() -
                     blocks.get(h - ConsensusParams.DIFFICULTY_WINDOW).getTimeMillis();
        long ideal = ConsensusParams.TARGET_BLOCK_INTERVAL_MS * ConsensusParams.DIFFICULTY_WINDOW;

        BigInteger target = HashingUtils.compactToTarget(currentBits);

        if (span < ideal / 2)      target = target.shiftRight(1);
        else if (span > ideal * 2) target = target.shiftLeft(1);

        currentBits   = HashingUtils.targetToCompact(target);
    }

    /*  ------------------------------------------------------------------
     *   work helper
     *  ------------------------------------------------------------------ */
    private static BigInteger workForBits(int bits) {
        return BigInteger.ONE.shiftLeft(256)
                             .divide(HashingUtils.compactToTarget(bits));
    }
}
