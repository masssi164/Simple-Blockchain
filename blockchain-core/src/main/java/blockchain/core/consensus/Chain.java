package blockchain.core.consensus;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import blockchain.core.crypto.HashingUtils;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe blockchain that maintains *all* seen blocks (DAG),
 * automatically switches to the branch with the greatest cumulative
 * Proof-of-Work and rebuilds the UTXO set on each re-organisation.
 *
 * Public API is identical to the former single-chain implementation, so
 * no outside class has to change.
 */
@Slf4j
public class Chain {

    /* ──────────────────────────────────────────────────────────────── */
    /*  global DAG structures                                           */
    /* ──────────────────────────────────────────────────────────────── */
    private final Map<String, Block>       allBlocks       = new ConcurrentHashMap<>();
    private final Map<String, BigInteger>  cumulativeWork  = new ConcurrentHashMap<>();
    private final Map<String, String>      parent          = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Block> activeChain  = new CopyOnWriteArrayList<>();
    private final Map<String, TxOutput>    utxo            = new ConcurrentHashMap<>();

    private volatile String bestTipHash;
    private int             currentBits     = 0x1f0fffff;          // easy PoW for demos

    /* ──────────────────────────────────────────────────────────────── */
    /*  ctor – build hard-coded genesis and index it                    */
    /* ──────────────────────────────────────────────────────────────── */
    public Chain() {
        Block g = genesisBlock();
        indexBlock(g);
        switchActiveChain(g.getHashHex());              // also fills UTXO
    }

    /* ──────────────────────────────────────────────────────────────── */
    /*  public read-only helpers                                        */
    /* ──────────────────────────────────────────────────────────────── */
    public Block                getLatest()        { return activeChain.get(activeChain.size() - 1); }
    public List<Block>          getBlocks()        { return List.copyOf(activeChain); }
    public Map<String, TxOutput> getUtxoSnapshot() { return Map.copyOf(utxo); }
    public BigInteger           getTotalWork()     { return cumulativeWork.get(bestTipHash); }

    /* ──────────────────────────────────────────────────────────────── */
    /*  main entry – append a block (may trigger re-org)                */
    /* ──────────────────────────────────────────────────────────────── */
    public void addBlock(Block b) {

        /* 0) quick parent sanity – unknown parent ⇒ same message as before */
        if (!"0".repeat(64).equals(b.getPreviousHashHex()) &&
            !allBlocks.containsKey(b.getPreviousHashHex()))
            throw new BlockchainException("prev-hash mismatch");

        /* 1) intra-block transaction checks (same as before) */
        validateTxs(b);

        /* 2) Proof-of-Work */
        if (!b.isProofValid())
            throw new BlockchainException("invalid PoW");

        /* 3) insert into global DAG                                            */
        indexBlock(b);

        /* 4) switch active branch if this fork is now heavier                  */
        BigInteger newWork  = cumulativeWork.get(b.getHashHex());
        BigInteger bestWork = cumulativeWork.get(bestTipHash);
        if (newWork.compareTo(bestWork) > 0) {
            log.info("⛓️  re-org → new tip {} (h={})  work={}",
                     b.getHashHex().substring(0, 8), b.getHeight(), newWork);
            switchActiveChain(b.getHashHex());
        }
    }

    /* ──────────────────────────────────────────────────────────────── */
    /*  genesis & helpers                                               */
    /* ──────────────────────────────────────────────────────────────── */
    private Block genesisBlock() {
        Wallet miner = new Wallet();
        Transaction cb = new Transaction(miner.getPublicKey(),
                                         ConsensusParams.blockReward(0));
        return new Block(0, "0".repeat(64),
                         List.of(cb), currentBits,
                         Instant.now().toEpochMilli(), 0);
    }

    private void validateTxs(Block b) {
        if (b.getTxList().isEmpty())           throw new BlockchainException("empty block");
        if (!b.getTxList().get(0).isCoinbase()) throw new BlockchainException("first tx must be coinbase");
        b.getTxList().stream().skip(1).forEach(tx -> {
            if (!tx.verifySignatures()) throw new BlockchainException("bad signature");
        });
    }

    /* ────────── DAG index ────────── */
    private void indexBlock(Block b) {
        String h = b.getHashHex();
        allBlocks.put(h, b);
        parent.put(h, b.getPreviousHashHex());

        BigInteger parentWork = "0".repeat(64).equals(b.getPreviousHashHex())
                ? BigInteger.ZERO
                : cumulativeWork.get(b.getPreviousHashHex());
        cumulativeWork.put(h, parentWork.add(workForBits(b.getCompactDifficultyBits())));
    }

    /* ────────── active-branch switch (re-org) ────────── */
    private synchronized void switchActiveChain(String newTipHash) {

        /* 1) collect blocks from new tip back to genesis */
        List<Block> branchRev = new ArrayList<>();
        String ptr = newTipHash;
        while (ptr != null && allBlocks.containsKey(ptr)) {
            branchRev.add(allBlocks.get(ptr));
            ptr = parent.get(ptr);
        }
        Collections.reverse(branchRev);   // genesis … newTip

        /* 2) rebuild UTXO */
        utxo.clear();
        branchRev.forEach(this::updateUtxo);

        /* 3) swap active list */
        activeChain.clear();
        activeChain.addAll(branchRev);

        bestTipHash = newTipHash;
    }

    private void updateUtxo(Block b) {
        for (Transaction tx : b.getTxList()) {
            int idx = 0;
            for (TxOutput out : tx.getOutputs()) {
                utxo.put(out.id(tx.calcHashHex(), idx++), out);
            }
        }
    }

    /* ────────── difficulty / work helpers ────────── */
    private static BigInteger workForBits(int bits) {
        return BigInteger.ONE.shiftLeft(256)
                             .divide(HashingUtils.compactToTarget(bits));
    }
}
